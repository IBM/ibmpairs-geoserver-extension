package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsGeoserverExtensionConfig {
    final static Logger logger = Logger.getLogger(PairsGeoserverExtensionConfig.class);
    public static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".pairsDataService");
    public static final String CONFIG_FILE = "pairsGeoserverExtensionConfig.json";
    private static PairsGeoserverExtensionConfig instance;
    public static String RASTER = "raster";
    public static String BUFFERED_IMAGE = "bufferedImage";

    // Pairs Http header names and query string key options from client
    public static String PAIRS_HEADER_KEY = "ibmpairs";
    public static String PAIRS_QUERY_STRING_KEY_PREFIX = "ibmpairs";
    public static String PAIRS_QUERY_KEY_LAYERID = PAIRS_QUERY_STRING_KEY_PREFIX + "_layerid";
    public static String PAIRS_QUERY_KEY_TIMESTAMP = PAIRS_QUERY_STRING_KEY_PREFIX + "_timestamp"; // epoch in seconds
    public static String PAIRS_QUERY_KEY_STATISTIC = PAIRS_QUERY_STRING_KEY_PREFIX + "_statistic";

    /**
     * Dynamically configurable items
     * 
     * Note: To identify the location of the hbase-data-service use either
     * pairsBaseUrlStr or the individual components of the Uri. If pairsBaseUrlStr
     * is provided it must contain all components of the url except the query
     * parameters and will take precedence over the components.
     */
    private String pairsDataServiceBaseRasterUrl = "http://pairs-alpha.watson.ibm.com:9082/api/v1/dataquery/raster";

    private String pairsDataServiceScheme = "http";
    private String pairsDataServiceHostname = "pairs-alpha";
    private int pairsDataServicePort = 9082;
    private String getMapRasterAction = "api/v1/dataquery/raster";

    private int pairsTestLayerId = 49180;
    private long pairsTestLayerTimestamp = 1435708800L;
    private String pairsTestStatistic = "mean";

    public String[] listOfCreateCoverage2dMethods = { RASTER, BUFFERED_IMAGE };
    public String[] listOfCreateBufferedImageMethods = { "getGrayImageFromFloatData", "getGrayImageFromIntData",
            "default" }; // default same as "getGrayImageFromFloatData"
    public String createCoverage2DMethod = RASTER; // When "raster" the BufferedImage generator is not used
    public String createBufferedImageMethod = "getGrayImageFromFloatData";

    public static void main(String[] args) {
        PairsGeoserverExtensionConfig pc = getInstance();
        logger.info(pc.toString());
        try {
            writeToFileSystem();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static PairsGeoserverExtensionConfig getInstance() {
        PairsGeoserverExtensionConfig prevInstance = instance;

        if (instance == null)
            instance = readFromResources();
        if (instance == null)
            instance = readFromFileSystem();
        if (instance == null) {
            logger.warn("Using default instance of config from class constants");    
            instance = new PairsGeoserverExtensionConfig();
        }

        if(prevInstance == null)
            logger.info(instance.toString());

        return instance;
    }

    private PairsGeoserverExtensionConfig() {
    }

    private static PairsGeoserverExtensionConfig readFromResources() {
        PairsGeoserverExtensionConfig result = null;
        Path path = null;
        try {
            path = Paths.get(PairsGeoserverExtensionConfig.class.getClassLoader().getResource(CONFIG_FILE).toURI());
            result = deserializeFile(path, PairsGeoserverExtensionConfig.class);
            logger.info("Config: " + CONFIG_FILE + ", read from resource path url: " + path.toString());
        } catch (NullPointerException | IOException | URISyntaxException e) {
            logger.error("Config: " + CONFIG_FILE + ", Not found on resource classpath; msg: " + e.getMessage());
        }

        return result;
    }

    private static PairsGeoserverExtensionConfig readFromFileSystem() {
        PairsGeoserverExtensionConfig result = null;
        Path path = Paths.get(CONFIG_PATH.toString(), CONFIG_FILE);
        try {
            result = deserializeFile(path, PairsGeoserverExtensionConfig.class);
            logger.info("Config: " + CONFIG_FILE + ", read from file system path: " + path.toString());
        } catch (NullPointerException | IOException e) {
            logger.error("Config: " + CONFIG_FILE + ", Not found on file system path: " + path.toString() + ", msg: " + e.getMessage());
        }

        return result;
    }

    private static void writeToFileSystem() throws JsonGenerationException, JsonMappingException, IOException {
        Path path = CONFIG_PATH;
        Files.createDirectories(path);
        path = path.resolve(CONFIG_FILE);
        path.toFile().createNewFile();
        serializeToFile(path.toFile(), instance);
    }

    public String[] getListOfCreateCoverage2dMethods() {
        return this.listOfCreateCoverage2dMethods;
    }

    @Override
    public String toString() {
        return "PairsGeoserverExtensionConfig [getMapRasterAction=" + getMapRasterAction
                + ", pairsDataServiceBaseRasterUrl=" + pairsDataServiceBaseRasterUrl + ", pairsDataServiceHostname="
                + pairsDataServiceHostname + ", pairsDataServicePort=" + pairsDataServicePort
                + ", pairsDataServiceScheme=" + pairsDataServiceScheme + ", pairsTestLayerId=" + pairsTestLayerId
                + ", pairsTestLayerTimestamp=" + pairsTestLayerTimestamp + ", pairsTestStatistic=" + pairsTestStatistic
                + "]";
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

    public void setListOfCreateCoverage2dMethods(String[] listOfCreateCoverage2dMethods) {
        this.listOfCreateCoverage2dMethods = listOfCreateCoverage2dMethods;
    }

    public String[] getListOfCreateBufferedImageMethods() {
        return this.listOfCreateBufferedImageMethods;
    }

    public void setListOfCreateBufferedImageMethods(String[] listOfCreateBufferedImageMethods) {
        this.listOfCreateBufferedImageMethods = listOfCreateBufferedImageMethods;
    }

    public String getCreateCoverage2DMethod() {
        return this.createCoverage2DMethod;
    }

    public void setCreateCoverage2DMethod(String createCoverage2DMethod) {
        this.createCoverage2DMethod = createCoverage2DMethod;
    }

    public String getCreateBufferedImageMethod() {
        return this.createBufferedImageMethod;
    }

    public void setCreateBufferedImageMethod(String createBufferedImageMethod) {
        this.createBufferedImageMethod = createBufferedImageMethod;
    }

    public String getPairsDataServiceHostname() {
        return this.pairsDataServiceHostname;
    }

    public void setPairsDataServiceHostname(String pairsDataServiceHostname) {
        this.pairsDataServiceHostname = pairsDataServiceHostname;
    }

    public int getPairsDataServicePort() {
        return this.pairsDataServicePort;
    }

    public void setPairsDataServicePort(int pairsDataServicePort) {
        this.pairsDataServicePort = pairsDataServicePort;
    }

    public int getPairsTestLayerId() {
        return this.pairsTestLayerId;
    }

    public void setPairsTestLayerId(int pairsTestLayerId) {
        this.pairsTestLayerId = pairsTestLayerId;
    }

    public long getPairsTestLayerTimestamp() {
        return this.pairsTestLayerTimestamp;
    }

    public void setPairsTestLayerTimestamp(long pairsTestLayerTimestamp) {
        this.pairsTestLayerTimestamp = pairsTestLayerTimestamp;
    }

    public String getPairsTestStatistic() {
        return this.pairsTestStatistic;
    }

    public void setPairsTestStatistic(String pairsTestStatistic) {
        this.pairsTestStatistic = pairsTestStatistic;
    }

    public String getGetMapRasterAction() {
        return getMapRasterAction;
    }

    public void setGetMapRasterAction(String getMapRasterAction) {
        this.getMapRasterAction = getMapRasterAction;
    }

    public String getPairsDataServiceScheme() {
        return pairsDataServiceScheme;
    }

    public void setPairsDataServiceScheme(String pairsDataServiceScheme) {
        this.pairsDataServiceScheme = pairsDataServiceScheme;
    }

    public String getPairsDataServiceBaseRasterUrl() {
        return pairsDataServiceBaseRasterUrl;
    }

    public void setPairsDataServiceBaseRasterUrl(String pairsDataServiceBaseUrl) {
        this.pairsDataServiceBaseRasterUrl = pairsDataServiceBaseUrl;
    }

}