package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.media.jai.DataBufferFloat;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

import com.ibm.pa.utils.PairsUtilities;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;

/**
 * This class is nominally meant to retrieve data for a single
 * pairslayer/dimension combination based on the layers specified in the
 * PairsWMSQueryParam queryParams. There is a static utility method that will
 * combine multiple layers into a single multibanded coverage result.
 * 
 * 
 * *** Comments on creating 'multilayer/multiband' Coverage2D ****
 * 
 * The data from each referenced Pairs layer is added to a band in a
 * javax.media.jai.PlanarImage which is used to construct the GridCoverage2D
 * 
 * https://docs.geotools.org/latest/javadocs/org/geotools/coverage/grid/GridCoverage2D.html
 * Or look at source code to try to figure this out.
 * 
 * Notes on the final GridCoverage[] source field of GridCoverage constructor.
 * The source field is used as a tracking mechanism to show the coverage objects
 * used to create each band in the PlanarImage of the GridCoverage2D. This is
 * used if bands were added to the image by transformations to a initial
 * coverage such as changing the CRS, interpolation, .... GridCoverage2D[]
 * Source[] would typically be used when handed a coverage to see its history of
 * processing the bands.
 * 
 * https://docs.geotools.org/latest/javadocs/index.html?org/geotools/coverage/processing/CoverageProcessor.html
 * 
 */
public class PairsQueryCoverageJob implements Callable<GridCoverage2D> {
    private static final Logger logger = org.geotools.util.logging.Logging.getLogger(PairsQueryCoverageJob.class);
    PairsWMSQueryParam queryParams;
    PairsRasterRequest pairsRasterRequest;
    PairsCoverageReader pairsCoverageReader;
    String coverageName;
    GridCoverageFactory gridCoverageFactory;
    Boolean dataBufferOnly = false;
    ImageDescriptor responseImageDescriptor;
    Envelope2D responseEnvelope2D;
    float[] imageDataFloat;
    WritableRaster writableRaster;
    TiledImage tiledImage;
    GridCoverage2D gridCoverage2D;

    public PairsQueryCoverageJob(PairsWMSQueryParam queryParams, PairsRasterRequest pairsRasterRequest,
            PairsCoverageReader coverageReader, Boolean dataBufferOnly) {
        this.queryParams = queryParams;
        this.pairsRasterRequest = pairsRasterRequest;
        this.pairsCoverageReader = coverageReader;
        this.dataBufferOnly = dataBufferOnly;
    }

    @Override
    public GridCoverage2D call() throws Exception {
        getDataFromPairsDataService(pairsRasterRequest);
        if (dataBufferOnly)
            return null;

        writableRaster = buildRaster(responseImageDescriptor, imageDataFloat);
        gridCoverage2D = gridCoverageFactory.create(coverageName, writableRaster, responseEnvelope2D);

        return gridCoverage2D;
    }

    public void getDataFromPairsDataService(PairsRasterRequest rasterRequest)
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpResponse response = getHttpResponseFromPairsDataService(rasterRequest);

        String pairsHeaderJson = PairsUtilities.getResponseHeader(response,
                PairsGeoserverExtensionConfig.PAIRS_HEADER_KEY);
        responseImageDescriptor = PairsUtilities.deserializeJson(pairsHeaderJson, ImageDescriptor.class);
        logger.info("Response ImageDescriptor: " + responseImageDescriptor.toString());

        responseEnvelope2D = new Envelope2D(pairsCoverageReader.getCoordinateReferenceSystem(),
                responseImageDescriptor.getBoundingBox().getSwLonLat()[0],
                responseImageDescriptor.getBoundingBox().getSwLonLat()[1],
                responseImageDescriptor.getBoundingBox().getWidth(),
                responseImageDescriptor.getBoundingBox().getHeight());

        byte[] rawData = PairsUtilities.readRawContent(response);
        imageDataFloat = PairsUtilities.byteArray2FloatArray(rawData);
    }

    private WritableRaster buildRaster(ImageDescriptor responseImageDescriptor, float[] imageData) {
        int width = responseImageDescriptor.getWidth();
        int height = responseImageDescriptor.getHeight();
        javax.media.jai.DataBufferFloat dataBuffer = new DataBufferFloat(imageData, width * height);

        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        java.awt.image.WritableRaster writableRaster = RasterFactory.createWritableRaster(sampleModel, dataBuffer,
                new Point(0, 0));

        return writableRaster;
    }

    private TiledImage buildTiledImage(Raster raster) {
        ColorModel colorModel = PlanarImage.createColorModel(raster.getSampleModel());
        javax.media.jai.TiledImage tiledImage = new TiledImage(0, 0, raster.getWidth(), raster.getHeight(), 0, 0,
                raster.getSampleModel(), colorModel);
        tiledImage.setData(raster);
        return tiledImage;
    }

    /**
     * Method for testing out building multiband coverage
     * 
     * @return
     */
    private GridCoverage2D buildGridCoverage2D_2() {
        int width = responseImageDescriptor.getWidth();
        int height = responseImageDescriptor.getHeight();
        float[][] imageData = { imageDataFloat, imageDataFloat };
        javax.media.jai.DataBufferFloat dataBuffer = new DataBufferFloat(imageData, width * height);
        javax.media.jai.TiledImage tiledImage;
        GridCoverage2D result;

        // method1
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 2);
        java.awt.image.Raster raster = RasterFactory.createRaster(sampleModel, dataBuffer, new Point(0, 0));
        tiledImage = new TiledImage(0, 0, raster.getWidth(), raster.getHeight(), 0, 0, raster.getSampleModel(), null);
        tiledImage.setData(raster);
        result = gridCoverageFactory.create(coverageName, tiledImage, responseEnvelope2D);

        // method2
        // java.awt.image.WritableRaster writableRaster =
        // RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, width,
        // height, 2, null);
        // float[] f = new float[writableRaster.getNumBands()];
        // for (int i = 0; i < width; i++) {
        // for (int j = 0; j < height; j++) {
        // f[0] = imageData[0][j + height * i];
        // f[1] = imageData[1][j + height * i];
        // writableRaster.setPixel(i, j, f);
        // }
        // }

        // // writableRaster.setDataElements(0, 0, width, height, dataBuffer);
        // tiledImage = new TiledImage(0, 0, raster.getWidth(), raster.getHeight(), 0,
        // 0, raster.getSampleModel(), null);
        // tiledImage.setData(raster);
        // result = gridCoverageFactory.create(coverageName, tiledImage,
        // responseEnvelope2D);

        return result;
    }

    private HttpResponse getHttpResponseFromPairsDataService(PairsRasterRequest rasterRequest)
            throws URISyntaxException, ClientProtocolException, IOException {
        ImageDescriptor imageDescriptor = queryParams.getRequestImageDescriptor();
        HttpResponse response = null;

        URIBuilder builder = new URIBuilder(PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceBaseUrl()
                + "dataquery/" + "layer/raster");

        builder.setParameter("LEVEL", Integer.toString(queryParams.getLevel()))
                .setParameter("STATISTIC", queryParams.getStatistic())
                .setParameter("WIDTH", Integer.toString(imageDescriptor.getWidth()))
                .setParameter("HEIGHT", Integer.toString(imageDescriptor.getHeight()))
                .setParameter("BBOX", imageDescriptor.getBoundingBox().toString())
                .setParameter("IBMPAIRSLAYER", rasterRequest.getIbmpairsquery());

        HttpGet request = new HttpGet(builder.build());
        request.addHeader("accepts", "application/binary");

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceUid(),
                        PairsGeoserverExtensionConfig.getInstance().getPairsDataServicePw()));

        HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        response = httpClient.execute(request);

        return response;
    }

    /**
     * Deprecated
     * 
     * @param layerId
     * @return
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    private HttpResponse getHttpResponseFromPairsDataService_deprecate(Integer layerId)
            throws URISyntaxException, ClientProtocolException, IOException {
        ImageDescriptor imageDescriptor = queryParams.getRequestImageDescriptor();
        HttpResponse response = null;

        URIBuilder builder = new URIBuilder(PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceBaseUrl()
                + "dataquery/" + "layer/" + layerId + "/raster");

        builder.setParameter("timestamp", Long.toString(queryParams.getTimestamp()))
                .setParameter("level", Integer.toString(queryParams.getLevel()))
                .setParameter("statistic", queryParams.getStatistic())
                .setParameter("width", Integer.toString(imageDescriptor.getWidth()))
                .setParameter("height", Integer.toString(imageDescriptor.getHeight()))
                .setParameter("swlon", Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[0]))
                .setParameter("swlat", Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[1]))
                .setParameter("nelon", Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[0]))
                .setParameter("nelat", Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[1]));

        if ((queryParams.getDimension() != null && !queryParams.getDimension().isEmpty())
                && (queryParams.getDimensionValue() != null && !queryParams.getDimensionValue().isEmpty())) {
            builder.addParameter("dimension", queryParams.getDimension());
            builder.addParameter("dimensionvalue", queryParams.getDimensionValue());
        }

        HttpGet request = new HttpGet(builder.build());
        request.addHeader("accepts", "application/binary");

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceUid(),
                        PairsGeoserverExtensionConfig.getInstance().getPairsDataServicePw()));

        HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        response = httpClient.execute(request);

        return response;
    }

    /**
     * Deprecated. Use buildGridCoverage2D() instead which creates a PlanarImage
     * that can accept additional sources (databands)
     * 
     * This is the original, long-used method of PairsCoverageReader:read(...)
     * 
     * @param responseImageDescriptor
     * @param imageVector
     * @return
     */
    private GridCoverage2D buildGridCoverage2D(ImageDescriptor responseImageDescriptor, float[] imageVector) {
        GridCoverage2D result = null;

        float[][] raster = PairsUtilities.vector2array(imageVector, responseImageDescriptor.getWidth());
        result = pairsCoverageReader.getGridCoverageFactory().create(coverageName, raster, responseEnvelope2D);
        return result;
    }

    public Integer getLayerId() {
        return pairsRasterRequest.getPairsLayer().getId();
    }

    public Boolean getDataBufferOnly() {
        return dataBufferOnly;
    }

    public void setDataBufferOnly(Boolean dataBufferOnly) {
        this.dataBufferOnly = dataBufferOnly;
    }

    public ImageDescriptor getResponseImageDescriptor() {
        return responseImageDescriptor;
    }

    public void setResponseImageDescriptor(ImageDescriptor responseImageDescriptor) {
        this.responseImageDescriptor = responseImageDescriptor;
    }

    public Envelope2D getResponseEnvelope2D() {
        return responseEnvelope2D;
    }

    public void setResponseEnvelope2D(Envelope2D responseEnvelope2D) {
        this.responseEnvelope2D = responseEnvelope2D;
    }

    public float[] getImageDataFloat() {
        return imageDataFloat;
    }

    public void setImageDataFloat(float[] imageDataFloat) {
        this.imageDataFloat = imageDataFloat;
    }

    public WritableRaster getWritableRaster() {
        return writableRaster;
    }

    public void setWritableRaster(WritableRaster raster) {
        this.writableRaster = raster;
    }

    public TiledImage getTiledImage() {
        return tiledImage;
    }

    public void setTiledImage(TiledImage tiledImage) {
        this.tiledImage = tiledImage;
    }

    public GridCoverage2D getGridCoverage2D() {
        return gridCoverage2D;
    }

    public String getCoverageName() {
        return coverageName;
    }

    public GridCoverageFactory getGridCoverageFactory() {
        return gridCoverageFactory;
    }

    public void setGridCoverage2D(GridCoverage2D gridCoverage2D) {
        this.gridCoverage2D = gridCoverage2D;
    }
}
