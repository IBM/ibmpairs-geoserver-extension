package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsWMSQueryParam {
    static final Logger logger = Logger.getLogger(PairsWMSQueryParam.class.getName());
    int layerid;
    long timestamp;
    String statistic;
    int level = -1;
    String crs;
    ImageDescriptor requestImageDescriptor;
    String dimension;
    String dimensionValue;

    /**
     * Return the query value contained in the threadlocal query string TODO Its
     * client issues WMS request with ibmpairs specific query strings
     * &ibmpairs_layeid=49180&ibmpairs_timestamp=123456
     * 
     * Update: dec 2020, Dispatcher.REQUEST will be null on certain adminstrative
     * requests Like create data store. In this case a null is returned and the
     * PairsCoverageReader handles the null context for original bbox etc.
     * 
     * Update: Dec 2020 add simple suppport for single dimension and value.
     * 
     * TODO: Add general query support for multiple dimensions/values for multiple
     * by returning multiband tif in PairsCoverageReader
     * 
     * TODO: Add POST support
     * 
     * TODO: Improve error handling returned to client.
     * 
     */
    public static PairsWMSQueryParam getRequestQueryStringParameter() throws IllegalArgumentException {
        PairsWMSQueryParam queryParams = new PairsWMSQueryParam();

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            String msg = "Unable to retrieve ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get()";
            logger.info(msg);
            // throw new IllegalArgumentException(msg);
            return null;
        }

        Map<String, String> kvp = req.getRawKvp();
        Map<String, String> invalidParams = queryParams.validateParams(kvp);
        if (!invalidParams.isEmpty())
            throw new IllegalArgumentException(invalidParams.toString());

        queryParams.setLayerid(Integer.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_LAYERID)));
        queryParams.setTimestamp(Long.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_TIMESTAMP)));
        queryParams.setStatistic(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC));
        queryParams.setCrs(kvp.get("CRS"));

        String bboxStr = kvp.get("BBOX");
        StringTokenizer bboxTkn = new StringTokenizer(bboxStr, ",");
        double swlat = Double.parseDouble(bboxTkn.nextToken());
        double swlon = Double.parseDouble(bboxTkn.nextToken());
        double nelat = Double.parseDouble(bboxTkn.nextToken());
        double nelon = Double.parseDouble(bboxTkn.nextToken());
        int height = Integer.parseInt(kvp.get("HEIGHT"));
        int width = Integer.parseInt(kvp.get("WIDTH"));
        BoundingBox bbox = new BoundingBox(swlon, swlat, nelon, nelat);
        queryParams.setRequestImageDescriptor(new ImageDescriptor(bbox, height, width));

        queryParams.setDimension(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_DIMENSION));
        queryParams.setDimensionValue(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_DIMENSION_VALUE));
        
        return queryParams;
    }

    public Map<String, String> validateParams(Map<String, String> params) {
        Map<String, String> invalidParams = new HashMap<>();
        return invalidParams;
    }

    @Override
    public String toString() {
        String result = "Serialization to Json failed";
        try {
            result = PairsUtilities.serializeObject(this);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public PairsWMSQueryParam() {
    }

    public String getStatistic() {
        return this.statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public int getLayerid() {
        return this.layerid;
    }

    public void setLayerid(int layerid) {
        this.layerid = layerid;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ImageDescriptor getRequestImageDescriptor() {
        return requestImageDescriptor;
    }

    public void setRequestImageDescriptor(ImageDescriptor requestImageDescriptor) {
        this.requestImageDescriptor = requestImageDescriptor;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getDimensionValue() {
        return dimensionValue;
    }

    public void setDimensionValue(String dimensionValue) {
        this.dimensionValue = dimensionValue;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
