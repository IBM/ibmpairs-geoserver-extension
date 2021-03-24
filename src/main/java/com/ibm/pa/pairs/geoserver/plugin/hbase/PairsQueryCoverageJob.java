package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.awt.Point;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.media.jai.DataBufferFloat;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

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

public class PairsQueryCoverageJob implements Callable<GridCoverage2D> {
    private static final Logger logger = org.geotools.util.logging.Logging.getLogger(PairsQueryCoverageJob.class);
    PairsWMSQueryParam queryParams;
    PairsCoverageReader pairsCoverageReader;
    GridCoverageFactory gridCoverageFactory;
    String coverageName;
    ImageDescriptor responseImageDescriptor;
    Envelope2D responseEnvelope2D;
    float[] imageDataFloat;
    GridCoverage2D gridCoverage2D;
    RenderedImage planarImage;

    public PairsQueryCoverageJob(PairsWMSQueryParam queryParams, PairsCoverageReader coverageReader) {
        this.queryParams = queryParams;
        this.pairsCoverageReader = coverageReader;

        gridCoverageFactory = coverageReader.getGridCoverageFactory();
        coverageName = pairsCoverageReader.getSource().toString();
    }

    @Override
    public GridCoverage2D call() throws Exception {
        Integer layerId = queryParams.getLayerid();
        getDataFromPairsDataService(layerId);
        gridCoverage2D = buildGridCoverage2D();
        planarImage = gridCoverage2D.getRenderedImage();

        /**
         * TODO finish this section by merging the bands from the images into a multi
         * band coverage. I don't think below is correct. Also, need to return correct result in PairsCoverageReader.getBandCount()
         */
        if (queryParams.getLayerid2() != null) {
            layerId = queryParams.getLayerid2();
            getDataFromPairsDataService(layerId);
            Raster raster = buildRaster(responseImageDescriptor, imageDataFloat);
            TiledImage tiledImage = buildTiledImage(raster);
            tiledImage.addSink(planarImage);
            GridCoverage2D result = gridCoverageFactory.create(coverageName, tiledImage, responseEnvelope2D);
        }

        return gridCoverage2D;
    }

    public void getDataFromPairsDataService(Integer layerId) throws ClientProtocolException, URISyntaxException, IOException {
        HttpResponse response = getHttpResponseFromPairsDataService(layerId);

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

    private Raster buildRaster(ImageDescriptor responseImageDescriptor, float[] imageData) {
        int width = responseImageDescriptor.getWidth();
        int height = responseImageDescriptor.getHeight();
        javax.media.jai.DataBufferFloat dataBuffer = new DataBufferFloat(imageData, width * height);

        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, 1);
        java.awt.image.Raster raster = RasterFactory.createRaster(sampleModel, dataBuffer, new Point(0, 0));

        return raster;
    }

    private TiledImage buildTiledImage(Raster raster) {
        ColorModel colorModel = PlanarImage.createColorModel(raster.getSampleModel());
        javax.media.jai.TiledImage tiledImage = new TiledImage(0, 0, raster.getWidth(), raster.getHeight(), 0, 0,
                raster.getSampleModel(), colorModel);
        tiledImage.setData(raster);
        return tiledImage;
    }

    private GridCoverage2D buildGridCoverage2D() {
        Raster raster = buildRaster(responseImageDescriptor, imageDataFloat);
        TiledImage tiledImage = buildTiledImage(raster);
        GridCoverage2D result = gridCoverageFactory.create(coverageName, tiledImage, responseEnvelope2D);

        return result;
    }

    private HttpResponse getHttpResponseFromPairsDataService(Integer layerId)
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
}
