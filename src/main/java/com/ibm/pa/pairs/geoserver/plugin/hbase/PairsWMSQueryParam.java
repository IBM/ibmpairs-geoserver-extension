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
    ImageDescriptor requestImageDescriptor;

    /**
     * Return the query value contained in the threadlocal query string TODO Its
     * client issues WMS request with ibmpairs specific query strings
     * &ibmpairs_layeid=49180&ibmpairs_timestamp=123456
     * 
     * TODO: layer, timestamp required, throw illegalArgException if either, not
     * both. Also, Dispatcher.REQUEST should never be null Add method to verify
     * params present
     */
    public static PairsWMSQueryParam getRequestQueryStringParameter() throws IllegalArgumentException {
        PairsWMSQueryParam queryParams = new PairsWMSQueryParam();

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            String msg = "Unable to retrieve ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get()";
            logger.warning(msg);
            throw new IllegalArgumentException(msg);
        }

        Map<String, String> kvp = req.getRawKvp();
        Map<String, String> invalidParams = queryParams.validateParams(kvp);
        if (!invalidParams.isEmpty())
            throw new IllegalArgumentException(invalidParams.toString());

        queryParams.setLayerid(Integer.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_LAYERID)));
        queryParams.setTimestamp(Long.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_TIMESTAMP)));
        queryParams.setStatistic(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC));
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
}
