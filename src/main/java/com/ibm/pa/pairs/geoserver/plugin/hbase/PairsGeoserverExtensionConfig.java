package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ibm.pa.utils.PairsUtilities;

import org.apache.http.client.utils.URIBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsGeoserverExtensionConfig {
    private static Logger logger = Logger.getLogger(PairsGeoserverExtensionConfig.class.getName());

    public static final String CONFIG_FILE = "pairsGeoserverExtensionConfig.json";
    private static PairsGeoserverExtensionConfig instance;
    public static String RASTER = "raster";
    public static String LEVEL = "level";
    public static String BUFFERED_IMAGE = "bufferedImage";

    // Pairs Http header names and query string key options from client
    public static String PAIRS_HEADER_KEY = "ibmpairs";
    public static String PAIRS_QUERY_STRING_KEY_PREFIX = "ibmpairs";
    public static String PAIRS_QUERY_KEY_LAYERID = PAIRS_QUERY_STRING_KEY_PREFIX + "_layerid";
    public static String PAIRS_QUERY_KEY_LAYERID2 = PAIRS_QUERY_STRING_KEY_PREFIX + "_layerid2";
    public static String PAIRS_QUERY_KEY_TIMESTAMP = PAIRS_QUERY_STRING_KEY_PREFIX + "_timestamp";
    public static String PAIRS_QUERY_KEY_STATISTIC = PAIRS_QUERY_STRING_KEY_PREFIX + "_statistic";
    public static String PAIRS_QUERY_KEY_DIMENSION = PAIRS_QUERY_STRING_KEY_PREFIX + "_dimension";
    public static String PAIRS_QUERY_KEY_DIMENSION_VALUE = PAIRS_QUERY_STRING_KEY_PREFIX + "_dimension_value";

    /**
     * Dynamically configurable items
     * 
     * Note: To identify the location of the hbase-data-service use either
     * pairsBaseUrlStr or the individual components of the Uri. If pairsBaseUrlStr
     * is provided it must contain all components of the url except the query
     * parameters and will take precedence over the components
     * 
     * to ignore in PairsGeo...Config.json use: "pairsDataServiceBaseRasterUrl": ""
     * 
     */
    private String pairsDataServiceBaseUrl = "https://pairs.res.ibm.com:8080/pairsdataservice/v2/";
    private String pairsDataServiceUid = "";
    private String pairsDataServicePw = "";

    private int pairsTestLayerId = 49180;
    private long pairsTestLayerTimestamp = 1435708800L;
    private String pairsTestStatistic = "mean";

    public String[] listOfCreateCoverage2dMethods = { RASTER, BUFFERED_IMAGE };
    public String[] listOfCreateBufferedImageMethods = { "getGrayImageFromFloatData", "getGrayImageFromIntData",
            "default" }; // default same as "getGrayImageFromFloatData"
    public String createCoverage2DMethod = RASTER; // When "raster" the BufferedImage generator is not used
    public String createBufferedImageMethod = "getGrayImageFromFloatData";

    /**
     * To run from cmd line. Here are 3 options. Note: 3) requires a mvn package to
     * collect all the jars in WEB-INF/lib
     * 
     * mvn exec:exec -Dexec.executable="java" -Dexec.args="-cp %classpath:.
     * com.ibm.pa.pairs.geoserver.plugin.hbase.PairsGeoserverExtensionConfig"
     */
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
        if (instance != null)
            return instance;

        if (instance == null)
            instance = readFromResources();
        if (instance == null) {
            instance = new PairsGeoserverExtensionConfig();
            logger.warning("Using default config from class constructor");
        }

        checkFields(instance);
        logger.info(instance.toString());

        return instance;
    }

    private PairsGeoserverExtensionConfig() {
    }

    /**
     * Verify the fields. Fix missing '/' this should be verified in
     * routines using URL
     * 
     * @param instance
     */
    private static void checkFields(PairsGeoserverExtensionConfig instance) {
        if (!instance.pairsDataServiceBaseUrl.endsWith("/"))
            instance.pairsDataServiceBaseUrl += "/";
    }

    private static PairsGeoserverExtensionConfig readFromResources() {
        PairsGeoserverExtensionConfig result = null;
        Path path = null;
        try {
            path = Paths.get(PairsGeoserverExtensionConfig.class.getClassLoader().getResource(CONFIG_FILE).toURI());
            result = PairsUtilities.deserializeFile(path, PairsGeoserverExtensionConfig.class);
            logger.info("Config: " + CONFIG_FILE + ", read from resource path url: " + path.toString());
        } catch (NullPointerException | IOException | URISyntaxException e) {
            logger.info("Config: " + CONFIG_FILE + ", Not found on resource classpath; msg: " + e.getMessage());
        }

        return result;
    }

    private static void writeToFileSystem() throws JsonGenerationException, JsonMappingException, IOException {
        Path path = Paths.get(System.getProperty("user.home"), "pairsDataService");
        ;
        Files.createDirectories(path);
        path = path.resolve(CONFIG_FILE);
        path.toFile().createNewFile();
        PairsUtilities.serializeToFile(path.toFile(), instance);
    }

    public String[] getListOfCreateCoverage2dMethods() {
        return this.listOfCreateCoverage2dMethods;
    }

    @Override
    public String toString() {
        try {
            return PairsUtilities.serializeObjectPretty(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return e.toString();
        }
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

    public String getPairsDataServiceBaseUrl() {
        return pairsDataServiceBaseUrl;
    }

    public void setPairsDataServiceBaseUrl(String pairsDataServiceBaseUrl) {
        this.pairsDataServiceBaseUrl = pairsDataServiceBaseUrl;
    }

    public String getPairsDataServiceUid() {
        return pairsDataServiceUid;
    }

    public void setPairsDataServiceUid(String pairsDataServiceUid) {
        this.pairsDataServiceUid = pairsDataServiceUid;
    }

    public String getPairsDataServicePw() {
        return pairsDataServicePw;
    }

    public void setPairsDataServicePw(String pairsDataServicePw) {
        this.pairsDataServicePw = pairsDataServicePw;
    }

}