package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsGeoserverExtensionConfig {
    private static Logger logger = Logger.getLogger(PairsGeoserverExtensionConfig.class);

    public static final Path CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".pairsDataService");
    public static final String CONFIG_FILE = "pairsGeoserverExtensionConfig.json";
    private static PairsGeoserverExtensionConfig instance;
    public static String RASTER = "raster";
    public static String LEVEL = "level";
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
     * parameters and will take precedence over the components
     * 
     * to ignore in PairsGeo...Config.json use: "pairsDataServiceBaseRasterUrl": ""
     * 
     */
    private String pairsDataServiceBaseUrl = "https://pairs.res.ibm.com:8080/api/v1/dataquery/";
    private String pairsDataServiceScheme = "https";
    private String pairsDataServiceHostname = "pairs-alpha.res.ibm.com";
    private int pairsDataServicePort = 9082;
    private String pairsDataServiceRootAction = "api/v1/dataquery/";

    private int pairsTestLayerId = 49180;
    private long pairsTestLayerTimestamp = 1435708800L;
    private String pairsTestStatistic = "mean";

    public String[] listOfCreateCoverage2dMethods = { RASTER, BUFFERED_IMAGE };
    public String[] listOfCreateBufferedImageMethods = { "getGrayImageFromFloatData", "getGrayImageFromIntData",
            "default" }; // default same as "getGrayImageFromFloatData"
    public String createCoverage2DMethod = RASTER; // When "raster" the BufferedImage generator is not used
    public String createBufferedImageMethod = "getGrayImageFromFloatData";

    public URI getPairsDataServiceRootUri() throws URISyntaxException {
        URIBuilder builder;
        String configUri = getPairsDataServiceBaseUrl();
        if (configUri != null && !configUri.isEmpty())
            builder = new URIBuilder(configUri);
        else {
            builder = new URIBuilder();
            String scheme = getPairsDataServiceScheme();
            String host = PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceHostname();
            int port = PairsGeoserverExtensionConfig.getInstance().getPairsDataServicePort();
            String path = PairsGeoserverExtensionConfig.getInstance().getPairsDataServiceRootAction();
            builder.setScheme(scheme).setHost(host).setPort(port).setPath(path);
        }
        return builder.build();
    }


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
        if (instance == null)
            instance = readFromFileSystem();
        if (instance == null) {
            instance = new PairsGeoserverExtensionConfig();
            logger.warn("Using default config from class constructor");
        }

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
            result = PairsUtilities.deserializeFile(path, PairsGeoserverExtensionConfig.class);
            logger.info("Config: " + CONFIG_FILE + ", read from resource path url: " + path.toString());
        } catch (NullPointerException | IOException | URISyntaxException e) {
            logger.warn("Config: " + CONFIG_FILE + ", Not found on resource classpath; msg: " + e.getMessage());
        }

        return result;
    }

    private static PairsGeoserverExtensionConfig readFromFileSystem() {
        PairsGeoserverExtensionConfig result = null;
        Path path = Paths.get(CONFIG_PATH.toString(), CONFIG_FILE);
        try {
            result = PairsUtilities.deserializeFile(path, PairsGeoserverExtensionConfig.class);
            logger.info("Config: " + CONFIG_FILE + ", read from file system path: " + path.toString());
        } catch (NullPointerException | IOException e) {
            logger.warn("Config: " + CONFIG_FILE + ", Not found on file system path: " + path.toString() + ", msg: "
                    + e.getMessage());
        }

        return result;
    }

    private static void writeToFileSystem() throws JsonGenerationException, JsonMappingException, IOException {
        Path path = CONFIG_PATH;
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
            return PairsUtilities.serializeObject(this);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
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

    public String getPairsDataServiceRootAction() {
        return pairsDataServiceRootAction;
    }

    public void setPairsDataServiceRootAction(String pairsDataServiceRootAction) {
        this.pairsDataServiceRootAction = pairsDataServiceRootAction;
    }

    public String getPairsDataServiceScheme() {
        return pairsDataServiceScheme;
    }

    public void setPairsDataServiceScheme(String pairsDataServiceScheme) {
        this.pairsDataServiceScheme = pairsDataServiceScheme;
    }

    public String getPairsDataServiceBaseUrl() {
        return pairsDataServiceBaseUrl;
    }

    public void setPairsDataServiceBaseUrl(String pairsDataServiceBaseUrl) {
        this.pairsDataServiceBaseUrl = pairsDataServiceBaseUrl;
    }

}