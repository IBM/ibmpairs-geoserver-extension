package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.log4j.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsWMSQueryParam {
    static final Logger logger = Logger.getLogger(PairsWMSQueryParam.class.getName());
    int layerid;
    long timestamp;
    String statistic;
    float nodataValue = PairsGeoserverExtensionConfig.DEFAULT_NO_DATA;
    private boolean debugNodata = false;

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
        PairsWMSQueryParam queryParams = new PairsWMSQueryParam();

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            logger.info("ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get() is null, using defaults");
        } else {
            Map<String, String> kvp = req.getRawKvp();

            if (kvp.containsKey(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_LAYERID))
                queryParams.setLayerid(Integer.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_LAYERID)));
            if (kvp.containsKey(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_TIMESTAMP))
                queryParams.setTimestamp(Long.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_TIMESTAMP)));
            if (kvp.containsKey(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC))
                queryParams.setStatistic(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC));
            if (kvp.containsKey(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_NODATA_VALUE)) {
                queryParams.setNodataValue(Float.valueOf(kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_NODATA_VALUE)));
            }
        }

        return queryParams;
    }

    public PairsWMSQueryParam() {
        this(PairsGeoserverExtensionConfig.getInstance().getPairsTestLayerId(), PairsGeoserverExtensionConfig.getInstance().getPairsTestLayerTimestamp(),
                PairsGeoserverExtensionConfig.getInstance().getPairsTestStatistic());
    }

    public PairsWMSQueryParam(int layerid, long timestamp, String statistic) {
        this.layerid = layerid;
        this.timestamp = timestamp;
        this.statistic = statistic;
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

    public float getNodataValue() {
        return this.nodataValue;
    }

    public void setNodataValue(float nodataValue) {
        this.nodataValue = nodataValue;
        this.debugNodata = true;
    }

    public boolean isDebugNodata() {
        return this.debugNodata;
    }
}
