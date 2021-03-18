package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.PlanarImage;

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
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;

public class PairsQueryCoverageJob implements Callable<PairsQueryCoverageJob> {
    private static final Logger logger = org.geotools.util.logging.Logging.getLogger(PairsQueryCoverageJob.class);
    PairsWMSQueryParam queryParams;
    PairsCoverageReader pairsCoverageReader;
    Integer layerIndex = 0;
    ImageDescriptor responseImageDescriptor;
    GridCoverage2D gridCoverage2D;
    RenderedImage renderedImage;
    PlanarImage planarImage;

    public PairsQueryCoverageJob(PairsWMSQueryParam queryParams, PairsCoverageReader coverageReader, int layerIndex) {
        this.queryParams = queryParams;
        this.pairsCoverageReader = coverageReader;
        this.layerIndex = layerIndex;
    }

    @Override
    public PairsQueryCoverageJob call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public void getDataFromPairsDataService() throws ClientProtocolException, URISyntaxException, IOException {
        HttpResponse response = getHttpResponseFromPairsDataService();

        String pairsHeaderJson = PairsUtilities.getResponseHeader(response,
                PairsGeoserverExtensionConfig.PAIRS_HEADER_KEY);
        ImageDescriptor responseImageDescriptor = PairsUtilities.deserializeJson(pairsHeaderJson,
                ImageDescriptor.class);
        logger.info("Response ImageDescriptor: " + responseImageDescriptor.toString());

        byte[] rawData = PairsUtilities.readRawContent(response);
        float[] imageDataFloat = PairsUtilities.byteArray2FloatArray(rawData);

        gridCoverage2D = buildGridCoverage2D(responseImageDescriptor, imageDataFloat);
    }

    private GridCoverage2D buildGridCoverage2D(ImageDescriptor responseImageDescriptor, float[] imageVector) {
        GridCoverage2D result = null;

        Envelope2D responseEnvelope = new Envelope2D(pairsCoverageReader.getCoordinateReferenceSystem(),
                responseImageDescriptor.getBoundingBox().getSwLonLat()[0],
                responseImageDescriptor.getBoundingBox().getSwLonLat()[1],
                responseImageDescriptor.getBoundingBox().getWidth(),
                responseImageDescriptor.getBoundingBox().getHeight());

            float[][] raster = PairsUtilities.vector2array(imageVector, responseImageDescriptor.getWidth());
            String coverageName = pairsCoverageReader.getSource().toString();
            result = pairsCoverageReader.getGridCoverageFactory().create(coverageName, raster, responseEnvelope);
        

        return result;
    }

    RenderedImage buildRenderedImage(ImageDescriptor responseImageDescriptor, float[] imageVector){
        BufferedImage image = getImage(responseImageDescriptor, imageVector, "default");
        result = coverageFactory.create(coverageName, image, responseEnvelope);


        return renderedImage;
    }

    private HttpResponse getHttpResponseFromPairsDataService()
            throws URISyntaxException, ClientProtocolException, IOException {
        ImageDescriptor imageDescriptor = queryParams.getRequestImageDescriptor();
        HttpResponse response = null;

        int layerId = layerIndex == 0 ? queryParams.getLayerid() : queryParams.getLayerid2();

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
}
