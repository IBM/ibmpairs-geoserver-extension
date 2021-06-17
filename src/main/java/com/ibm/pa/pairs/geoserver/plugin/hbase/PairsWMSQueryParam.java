package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ibm.pa.utils.JsonSerializable;
import com.ibm.pa.utils.PairsUtilities;

/**
 * Return the query value contained in the threadlocal query string TODO Its
 * client issues WMS request with ibmpairs specific query strings
 * &ibmpairs_layeid=49180&ibmpairs_timestamp=123456
 * 
 * Update: dec 2020, Dispatcher.REQUEST will be null on certain adminstrative
 * requests Like create data store. In this case a null is returned and the
 * PairsCoverageReader handles the null context for original bbox etc.
 * 
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsWMSQueryParam implements JsonSerializable {
    static final Logger logger = Logger.getLogger(PairsWMSQueryParam.class.getName());
    String service;
    String version;
    String request;
    String statistic;
    int level = -1;
    String crs;
    String ibmpairslayer;
    PairsLayerRequestType[] layers;
    ImageDescriptor requestImageDescriptor;

    public PairsWMSQueryParam(Map<String, Object> kvp, Map<String, String[]> httpRequestParamMap) throws Exception {
        Map<String, String> invalidParams;

        invalidParams = validateParams(kvp);
        if (!invalidParams.isEmpty())
            throw new IllegalArgumentException(invalidParams.toString());

        setStatistic((String) kvp.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC));
        setCrs((String) kvp.get("CRS"));

        // Build the request image descriptor
        String bboxStr = (String) kvp.get("BBOX");
        StringTokenizer bboxTkn = new StringTokenizer(bboxStr, ",");
        double swlat = Double.parseDouble(bboxTkn.nextToken());
        double swlon = Double.parseDouble(bboxTkn.nextToken());
        double nelat = Double.parseDouble(bboxTkn.nextToken());
        double nelon = Double.parseDouble(bboxTkn.nextToken());
        int height = Integer.parseInt((String) kvp.get("HEIGHT"));
        int width = Integer.parseInt((String) kvp.get("WIDTH"));
        BoundingBox bbox = new BoundingBox(swlon, swlat, nelon, nelat);
        setRequestImageDescriptor(new ImageDescriptor(bbox, height, width));

        // Build the requested layers
        layers = createRequestlayers(ibmpairslayer);
    }

    public static PairsWMSQueryParam buildPairsWMSQueryParam() {
        Map<String, Object> kvp = null;
        Map<String, String[]> httpRequestParamMap = null;

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            // see comments above about update: dec 2020
            String msg = "Unable to retrieve ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get()";
            logger.info(msg);
            // throw new IllegalArgumentException(msg);
            return null;
        }

        httpRequestParamMap = req.getHttpRequest().getParameterMap();
        kvp = req.getRawKvp();
        PairsWMSQueryParam result = new PairsWMSQueryParam(kvp, httpRequestParamMap);
        return result;
    }

    private PairsLayerRequestType[] createRequestlayers(String json) throws Exception {
        PairsLayerRequestType[] layers = PairsLayerRequestType.buildFromJson(json);
        return layers;
    }

    public Map<String, String> validateParams(Map<String, Object> params) {
        Map<String, String> invalidParams = new HashMap<>();
        return invalidParams;
    }

    @Override
    public String toString() {
        String result = "Serialization PairsQueryParams to Json failed";
        try {
            result = serialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public PairsLayerRequestType[] getLayers() {
        return this.layers;
    }

    public String getStatistic() {
        return this.statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
