package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsWMSQueryParam {
    static final Logger logger = Logger.getLogger(PairsWMSQueryParam.class.getName());
    int layerid;
    long timestamp;
    String statistic;

    @Override
    public String toString() {
        String result = "";
        try {
            result = PairsUtilities.serializeObject(this);
        } catch (Exception e) {
            result = "{" + " layerid='" + getLayerid() + "'" + ", timestamp='" + getTimestamp() + "'" + ", statistic='"
                    + getStatistic() + "'" + "}";
        }
        return result;
    }

    /**
     * Return the query value contained in the threadlocal query string TODO Its
     * client issues WMS request with ibmpairs specific query strings
     * &ibmpairs_layeid=49180&ibmpairs_timestamp=123456
     * 
     * TODO: Both layer and timestamp required, throw illegalArgException if either,
     * not both. Also, Dispatcher.REQUEST should never be null
     */
    public static PairsWMSQueryParam getRequestQueryStringParameter() {
        PairsWMSQueryParam queryParams = null;

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            logger.warning("Unable to retrieve ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get()");
        } else {
            queryParams = new PairsWMSQueryParam();
            Map<String, String> kvp = req.getRawKvp();

            if (kvp.containsKey(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_LAYERID))
                queryParams.setLayerid(Integer.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_LAYERID)));
            if (kvp.containsKey(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_TIMESTAMP))
                queryParams
                        .setTimestamp(Long.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_TIMESTAMP)));
            if (kvp.containsKey(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC))
                queryParams.setStatistic(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC));
        }

        return queryParams;
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
}
