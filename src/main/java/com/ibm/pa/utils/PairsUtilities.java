package com.ibm.pa.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.pa.pairs.geoserver.plugin.hbase.BoundingBox;
import com.ibm.pa.pairs.geoserver.plugin.hbase.PairsImageDescriptor;
import com.ibm.pa.pairs.geoserver.plugin.hbase.PairsGeoserverExtensionConfig;
import com.ibm.pa.pairs.geoserver.plugin.hbase.PairsRasterRequest;
import com.ibm.pa.pairs.geoserver.plugin.hbase.PairsWMSQueryParam;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class PairsUtilities {
    public static Logger logger = Logger.getLogger(PairsUtilities.class.getName());
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm");
    public static String DFORMAT = "%.4f";
    public static String IFORMAT = "%d";
    public static String LFORMAT = "%d";
    public static String PAIRS_DATA_SERVICE_HEADER_KEY = "ibmpairs";
    public static int TEST_LAYERID = 49180;
    public static long TEST_TIMESTAMP = 1435708800L;
    public static BoundingBox bbox = new BoundingBox(-80, 30, -70, 40);
    public static PairsImageDescriptor TEST_IMAGE_DESCRIPTOR = new PairsImageDescriptor(bbox, 384, 768);

    static class ResultWrapper {
        public int value;
    };

    public static void main(String[] args) {
        byte[] b = new byte[] { 0x0A, 0x1A, 0x2A, 0x3A, 0x4A, 0x5A, 0x6, 0x7 };
        System.out.println(Arrays.toString(PairsUtilities.byteArray2FloatArray(b)));

        try {
            // logger.info("resolution as level: " + (deserializeJson("{\"value\": 23}",
            // ResultWrapper.class).value));
            testComputePairsResolution();
            // testHbaseServiceCall();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void testComputePairsResolution() throws ClientProtocolException, IOException, URISyntaxException {
        PairsGeoserverExtensionConfig.getInstance()
                .setPairsDataServiceBaseUrl("https://pairs-alpha.res.ibm.com:8080/api/v1/dataquery/");
        PairsImageDescriptor imageDescriptor = new PairsImageDescriptor(new double[] { -90, 50, -80, 60 }, 256, 128);
        int layerId = 51;
        String statistic = "";

        double r = getPairsResolution(layerId, statistic, imageDescriptor);
        logger.info("resolution: " + r);
    }

    public static HttpResponse getHttpResponseFromPairsDataService(PairsRasterRequest rasterRequest)
            throws URISyntaxException, ClientProtocolException, IOException {
        PairsImageDescriptor imageDescriptor = rasterRequest.getRequestImageDescriptor();
        HttpResponse response = null;

        URIBuilder builder = new URIBuilder(
                PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceBaseUrl() + "dataquery/layer/raster");

        builder.setParameter("level", Integer.toString(rasterRequest.getPairsLayer().getPixelLevel()))
                .setParameter("statistic", rasterRequest.getStatistic())
                .setParameter("width", Integer.toString(imageDescriptor.getWidth()))
                .setParameter("height", Integer.toString(imageDescriptor.getHeight()))
                .setParameter("bbox", imageDescriptor.getBoundingBox().toQueryParam())
                .setParameter("ibmpairslayer", rasterRequest.getIbmpairsquery());

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
     * Deprecated March 2021, replace by methods in class PairsQueryCoverageJob, see
     * public void getDataFromPairsDataService(Integer layerId).
     * 
     * Build uri to return a raster from pairs-data-service:
     * 
     * "http://pairs.res.ibm.com:8080/api/v1/dataquery?layerid=49180&timestamp=1435708800&
     * swlat=30.0&swlon=-80.0&nelat=40.712&nelon=-70.0060&height=128&width=256";
     * 
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     * @throws URISyntaxException
     */
    /*
     * public static HttpResponse getRasterFromPairsDataService(PairsWMSQueryParam
     * queryParams, ImageDescriptor imageDescriptor) throws ClientProtocolException,
     * IOException, URISyntaxException { HttpResponse response = null;
     * 
     * URIBuilder builder = new
     * URIBuilder(PairsGeoserverExtensionConfig.getInstance().
     * getPairsDataServiceBaseUrl() + "dataquery/" + "layer/" +
     * queryParams.getLayerid() + "/raster");
     * 
     * builder.setParameter("timestamp", Long.toString(queryParams.getTimestamp()))
     * .setParameter("level", Integer.toString(queryParams.getLevel()))
     * .setParameter("statistic", queryParams.getStatistic()) .setParameter("width",
     * Integer.toString(imageDescriptor.getWidth())) .setParameter("height",
     * Integer.toString(imageDescriptor.getHeight())) .setParameter("swlon",
     * Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[0]))
     * .setParameter("swlat",
     * Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[1]))
     * .setParameter("nelon",
     * Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[0]))
     * .setParameter("nelat",
     * Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[1]));
     * 
     * if ((queryParams.getDimension() != null &&
     * !queryParams.getDimension().isEmpty()) && (queryParams.getDimensionValue() !=
     * null && !queryParams.getDimensionValue().isEmpty())) {
     * builder.addParameter("dimension", queryParams.getDimension());
     * builder.addParameter("dimensionvalue", queryParams.getDimensionValue()); }
     * 
     * HttpGet request = new HttpGet(builder.build()); request.addHeader("accepts",
     * "application/binary");
     * 
     * CredentialsProvider provider = new BasicCredentialsProvider();
     * provider.setCredentials(AuthScope.ANY, new
     * UsernamePasswordCredentials(PairsGeoserverExtensionConfig.getInstance().
     * getPairsDataServiceUid(),
     * PairsGeoserverExtensionConfig.getInstance().getPairsDataServicePw()));
     * 
     * HttpClient httpClient =
     * HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
     * response = httpClient.execute(request);
     * 
     * return response; }
     */

    public static Double getPairsResolution(int layerId, String statistic, PairsImageDescriptor imageDescriptor)
            throws ClientProtocolException, IOException, URISyntaxException {
        Double resolution = null;

        URIBuilder builder = new URIBuilder(PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceBaseUrl()
                + "dataquery/layer/" + layerId + "/level");

        builder.setParameter("width", Integer.toString(imageDescriptor.getWidth()))
                .setParameter("height", Integer.toString(imageDescriptor.getHeight()))
                .setParameter("swlon", Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[0]))
                .setParameter("swlat", Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[1]))
                .setParameter("nelon", Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[0]))
                .setParameter("nelat", Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[1]));

        HttpGet request = new HttpGet(builder.build());
        request.addHeader("accepts", "application/json");

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceUid(),
                        PairsGeoserverExtensionConfig.getInstance().getPairsDataServicePw()));

        HttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

        HttpResponse response = httpClient.execute(request);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            logger.info("request: " + request.getURI().toString() + ", status: "
                    + response.getStatusLine().getStatusCode() + ", " + response.getStatusLine().getReasonPhrase());
        } else {
            String json = EntityUtils.toString(response.getEntity());
            ResultWrapper rw = deserializeJson(json, ResultWrapper.class);
            resolution = 1e-06 * Math.pow(2, 29 - rw.value);
        }

        return resolution;
    }

    public static byte[] readRawContent(HttpResponse response)
            throws ClientProtocolException, IOException, URISyntaxException {
        long len = response.getEntity().getContentLength();
        InputStream is = response.getEntity().getContent();
        byte[] result = inputStream2ByteArray(is);
        is.close();

        if (result.length != len) {
            logger.severe(String.format("Error bytes read: %d != contentLength: %d", result.length, len));
        }

        return result;
    }

    public static String getResponseHeader(HttpResponse response, String key) {
        String result = "";
        Header[] headers = response.getHeaders(key);
        if (headers.length == 1)
            result = headers[0].getValue();
        return result;
    }

    public static byte[] inputStream2ByteArray(InputStream is) throws IOException {
        byte[] result = null;
        int bufferSize = (int) Math.pow(2, 16);
        byte[] data = new byte[bufferSize];
        int nRead;

        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while ((nRead = bis.read(data, 0, data.length)) != -1) {
            baos.write(data, 0, nRead);
        }
        result = baos.toByteArray();

        return result;
    }

    public static float[] byteArray2FloatArray(byte[] input) {
        float[] result = null;

        FloatBuffer fb = ByteBuffer.wrap(input).asFloatBuffer();
        if (fb.hasArray())
            result = fb.array();
        else {
            result = new float[fb.limit()];
            fb.get(result);
        }

        return result;
    }

    /**
     * Debugging routine, finds if there is any entry == nodataVal
     * 
     * @param float[] data
     * @param float   nodataVal
     * @return index of first nodata entry
     */
    public static int hasNodata(float[] data, float nodataVal) {
        int result = -1;

        for (int i = 0; i < data.length; i++) {
            if (data[i] == nodataVal) {
                result = i;
                break;
            }
        }

        return result;
    }

    public static float[][] vector2array(float[] vector, int width) {
        int index = 0;
        float[][] result = null;
        if (vector.length % width == 0) {
            int height = vector.length / width;
            result = new float[height][width];
            for (int rows = 0; rows < height; rows++) {
                index = rows * width;
                System.arraycopy(vector, index, result[rows], 0, width);
            }
        }
        return result;
    }

    // use cast for now but later scale
    public static int[] floatArray2IntArray(float[] input) {
        int[] result = new int[input.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) input[i];
        }
        return result;
    }

    /**
     * 
     * TODO: fix this method or remove Scaled to 0->255, NaN== -9999 set to 0
     * 
     * @param input
     * @return
     */
    public static int[] floatArray2ScaledIntArray(float[] input) {
        int[] result = new int[input.length];
        float[] minmax = getMinMax(input);
        int scale = 255;
        if (minmax[1] < 1e-06) // avoid div by 0
            minmax[1] = (float) 1e-06;

        for (int i = 0; i < result.length; i++) {
            if (input[i] < 0)
                result[i] = 0;
            else
                result[i] = (int) (scale * (input[i] - minmax[0]) / minmax[1]);
        }
        return result;
    }

    /**
     * min max for elements > 0
     */
    public static float[] getMinMax(float[] buffer) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] < 0)
                buffer[i] = 0;
            if (buffer[i] < min)
                min = buffer[i];
            if (buffer[i] > max)
                max = buffer[i];
        }
        return new float[] { min, max };
    }

    /**
     * ******************************* time manipulation
     ***********************************/

    /**
     * Notes: hyphen "-" or space accepted delimiter
     * 
     * if timeString is one number > 4 chars its assumed to be epoch secs and
     * converted to long
     * 
     * if timeString is null use current time
     * 
     * @param timeString yyyy-mm-dd-HH:mm:ss
     */
    public static Long toEpochSec(String timeString) {
        Long result = null;
        ZoneOffset offset = ZoneOffset.ofHours(0);

        if (timeString == null)
            return null;

        String input = timeString.replace("-", " ");

        if (!input.contains(" ")) {
            result = Long.valueOf(input);
        } else {
            if (!input.contains(":"))
                input += " 00:00";
            LocalDateTime localDateTime = LocalDateTime.parse(input, DEFAULT_DATE_TIME_FORMATTER);
            result = localDateTime.toEpochSecond(offset);
        }

        return result;
    }

    public static String toEpochSecString(String timeString) {
        Long epochSecs = toEpochSec(timeString);
        if (epochSecs == null)
            return null;
        else
            return epochSecs.toString();
    }

    public static String currentTimeAsString() {
        return DEFAULT_DATE_TIME_FORMATTER.format(LocalDateTime.now());
    }

    public static String currentTimeEpochSec() {
        ZoneOffset offset = ZoneOffset.ofHours(0);
        return Long.toString(LocalDateTime.now().toEpochSecond(offset));
    }

    /**
     * ******************************** Image testing
     */
    public static BufferedImage getTestImageIntRGB(int w, int h) {
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] rgbArray = new int[h * w];
        int scale = (int) Math.pow(2, 32);
        int ct = rgbArray.length;
        while (ct-- > 0) {
            rgbArray[ct] = (int) (Math.random() * scale);
        }

        result.setRGB(0, 0, w, h, rgbArray, 0, w);
        // final byte[] a = ((DataBufferByte)
        // result.getRaster().getDataBuffer()).getData();
        // System.arraycopy(rgbArray, 0, a, 0, h * w);
        return result;
    }

    public static BufferedImage getTestImageGrey(int w, int h) {
        BufferedImage result = null;

        byte[] grid = new byte[h * w];
        int scale = (int) Math.pow(2, 8);
        int ct = grid.length;
        while (ct-- > 0) {
            grid[ct] = (byte) (Math.random() * scale);
        }

        result = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        final byte[] a = ((DataBufferByte) result.getRaster().getDataBuffer()).getData();
        System.arraycopy(grid, 0, a, 0, h * w);
        return result;
    }

    private static BufferedImage getImageFloat2(int w, int h, int layerId, double latse, double lonse, double latne,
            double lonne) {
        int[] bandOffsets = { 0, 1, 2, 3 }; // length == bands, 0 == R, 1 == G, 2 == B and 3 == A
        /*
         * // Create a TYPE_FLOAT sample model (specifying how the pixels are stored)
         * SampleModel sampleModel = new
         * PixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, w, h, bands, w * bands,
         * bandOffsets); // ...and data buffer (where the pixels are stored) DataBuffer
         * buffer = new DataBufferFloat(w * h * bands);
         * 
         * // Wrap it in a writable raster WritableRaster raster =
         * Raster.createWritableRaster(sampleModel, buffer, null);
         * 
         * // Create a color model compatible with this sample model/raster (TYPE_FLOAT)
         * // Note that the number of bands must equal the number of color components in
         * the // color space (3 for RGB) + 1 extra band if the color model contains
         * alpha ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
         * ColorModel colorModel = new ComponentColorModel(colorSpace, true, false,
         * Transparency.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
         * 
         * // And finally create an image with this raster BufferedImage image = new
         * BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
         */
        return null;
    }

    /**
     * Create instance of targetClass from file
     * 
     * @param json
     * @return class instance
     * @throws IOException
     */
    public static <T> T deserializeFile(Path path, Class<T> targetClass) throws IOException {
        T instance = null;

        ObjectMapper mapper = new ObjectMapper();
        instance = mapper.readValue(path.toFile(), targetClass);

        return instance;
    }

    public static void serializeToFile(File file, Object object)
            throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, object);
    }

    public static <T> T deserializeJson(String json, Class<T> targetClass) throws IOException {
        T instance = null;

        ObjectMapper objectMapper = new ObjectMapper();
        instance = objectMapper.readValue(json, targetClass);

        return instance;
    }

    public static <T> String serializeObject(T input) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(input);
        return result;
    }

    public static <T> String serializeObjectPretty(T inputInstance) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputInstance);
        return result;
    }
}