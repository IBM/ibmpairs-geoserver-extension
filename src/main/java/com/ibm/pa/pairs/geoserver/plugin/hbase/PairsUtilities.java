package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

public class PairsUtilities {
    public static Logger logger = Logger.getLogger(PairsUtilities.class.getName());
    public static String DFORMAT = "%.4f";
    public static String IFORMAT = "%d";
    public static String LFORMAT = "%d";
    public static String PAIRS_DATA_SERVICE_HEADER_KEY = "ibmpairs";
    public static int TEST_LAYERID = 49180;
    public static long TEST_TIMESTAMP = 1435708800L;
    public static BoundingBox bbox = new BoundingBox(-80, 30, -70, 40);
    public static ImageDescriptor TEST_IMAGE_DESCRIPTOR = new ImageDescriptor(bbox, 384, 768);

    public static void main(String[] args) {
        byte[] b = new byte[] { 0x0A, 0x1A, 0x2A, 0x3A, 0x4A, 0x5A, 0x6, 0x7 };
        System.out.println(Arrays.toString(PairsUtilities.byteArray2FloatArray(b)));

        try {
            testHbaseServiceCall();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static BufferedImage testHbaseServiceCall() throws URISyntaxException, ClientProtocolException, IOException {
        URI uri = buildPairsDataServiceRasterUri(TEST_LAYERID, TEST_TIMESTAMP, -1, "Mean", TEST_IMAGE_DESCRIPTOR);
        HttpResponse response = getHttpResponse(uri);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);
            String msg = baos.toString("UTF-8");
            logger.severe(" status: " + response.getStatusLine().getStatusCode() + ", msg: " + msg);
            return null;
        }

        String pairsHeader = getResponseHeader(response, PAIRS_DATA_SERVICE_HEADER_KEY);
        logger.info("Pairs response header: " + pairsHeader);
        ImageDescriptor responseImageDescriptor = PairsUtilities.deserializeJson(pairsHeader, ImageDescriptor.class);
        byte[] rawData = readRawContent(response);
        logger.info("Pairs data length: " + rawData.length);
        float[] imageDataFloat = PairsUtilities.byteArray2FloatArray(rawData);
        int[] imageDataInt = PairsUtilities.floatArray2ScaledIntArray(imageDataFloat);

        BufferedImage image = new BufferedImage(responseImageDescriptor.getWidth(), responseImageDescriptor.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wr = image.getRaster();
        wr.setPixels(0, 0, responseImageDescriptor.getWidth(), responseImageDescriptor.getHeight(), imageDataInt);
        // DataBufferInt db = (DataBufferInt) wr.getDataBuffer();
        // int[] bufferedData = db.getData();
        // System.arraycopy(imageDataInt, 0, bufferedData, 0, imageDataInt.length);

        return image;
    }

    /**
     * BUild uri like
     * "http://pairs-web04:9082/api/v2/data?layerid=49180&timestamp=1435708800&
     * swlat=30.0&swlon=-80.0&nelat=40.712&nelon=-70.0060&height=128&width=256";
     * 
     * @return
     * @throws URISyntaxException
     */
    public static URI buildPairsDataServiceRasterUri(int layerId, long timestamp, int level, String statistic,
            ImageDescriptor imageDescriptor) throws URISyntaxException {
        URI result = null;
        URIBuilder builder = null;

        String configUri = PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceBaseRasterUrl();
        if (configUri != null && !configUri.isEmpty())
            builder = new URIBuilder(configUri);
        else {
            builder = new URIBuilder();
            String host = PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceHostname();
            int port = PairsGeoserverExtensionConfig.getInstance().getPairsDataServicePort();
            String path = PairsGeoserverExtensionConfig.getInstance().getGetMapRasterAction();
            builder.setScheme("http").setHost(host).setPort(port).setPath(path);
        }
        builder.setParameter("layerid", Integer.toString(layerId)).setParameter("timestamp", Long.toString(timestamp))
                .setParameter("level", Long.toString(level)).setParameter("statistic", statistic)
                .setParameter("width", Integer.toString(imageDescriptor.getWidth()))
                .setParameter("height", Integer.toString(imageDescriptor.getHeight()))
                .setParameter("swlon", Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[0]))
                .setParameter("swlat", Double.toString(imageDescriptor.getBoundingBox().getSwLonLat()[1]))
                .setParameter("nelon", Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[0]))
                .setParameter("nelat", Double.toString(imageDescriptor.getBoundingBox().getNeLonLat()[1]));

        result = builder.build();
        return result;
    }

    public static HttpResponse getHttpResponse(URI uri)
            throws ClientProtocolException, IOException, URISyntaxException {
        HttpResponse response = null;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);
        request.addHeader("accepts", "application/binary");
        response = httpClient.execute(request);
        logger.info("Response Code : " + response.getStatusLine().getStatusCode());
        return response;
    }

    public static byte[] readRawContent(HttpResponse response)
            throws ClientProtocolException, IOException, URISyntaxException {
        long len = response.getEntity().getContentLength();
        byte[] result = new byte[(int) len];

        // int nread = response.getEntity().getContent().read(result);
        InputStream is = response.getEntity().getContent();
        result = inputStream2ByteArray(is);

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

    /*
     * public static float[] getDataFloat(URI uri) throws ClientProtocolException,
     * IOException, URISyntaxException { byte[] rawData = getDataRaw(uri); float[]
     * floatData = byteArray2FloatArray(rawData); return floatData; }
     */
    public static byte[] inputStream2ByteArray(InputStream is) throws IOException {
        byte[] result = null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream((int) Math.pow(2, 21));
        int nRead;
        byte[] data = new byte[(int) Math.pow(2, 16)];
        BufferedInputStream bis = new BufferedInputStream(is);

        while ((nRead = bis.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        result = buffer.toByteArray();
        return result;
    }

    public static float[] byteArray2FloatArray(byte[] input) {
        float[] result = null;
        ByteBuffer bb = ByteBuffer.wrap(input);
        FloatBuffer fb = bb.asFloatBuffer();
        result = new float[fb.limit()];
        fb.get(result);
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
     * Image testing
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