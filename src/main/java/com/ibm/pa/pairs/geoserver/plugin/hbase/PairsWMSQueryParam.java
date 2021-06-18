package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ibm.pa.utils.HttpRequestParameterMap;
import com.ibm.pa.utils.JsonSerializable;

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

@JsonIgnoreProperties(ignoreUnknown = true, value = { "paramMap" })
public class PairsWMSQueryParam implements JsonSerializable {
    static final Logger logger = Logger.getLogger(PairsWMSQueryParam.class.getName());
    HttpRequestParameterMap paramMap;
    String statistic;
    int level = -1;
    String crs;
    String ibmpairslayer;
    PairsLayerRequestType[] layers;
    ImageDescriptor requestImageDescriptor;

    public PairsWMSQueryParam(final HttpRequestParameterMap paramMap) throws Exception {
        Map<String, String> invalidParams;
        this.paramMap = paramMap;

        invalidParams = validateParams(paramMap);
        if (!invalidParams.isEmpty())
            throw new IllegalArgumentException(invalidParams.toString());

        setStatistic(paramMap.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC));
        setCrs(paramMap.get("CRS"));
        setIbmpairslayer(paramMap.get(PairsGeoserverExtensionConfig.PAIRS_LAYER_QUERY_JSON));
        layers = PairsLayerRequestType.buildFromJson(ibmpairslayer);

        setRequestImageDescriptor(buildRequestImageDescriptor());
    }

    /**
     * Note, the optionalParams can be used to be backwards compatible with a query
     * that provides a IBMPAIRS_TIMESTAMP and IBMPAIRS_LAYERID instead of
     * IBMPAIRS__LAYERQUERY by build the json from the later from the former and
     * inserting the parameter into the optionalParams.
     * 
     * 
     * @param optionalParams - Used to add parameters if not on request,
     * @return
     * @throws Exception
     */
    public static PairsWMSQueryParam buildPairsWMSQueryParam(Map<String, String[]> optionalParams) throws Exception {
        Map<String, Object> kvp = null;
        HttpRequestParameterMap paramMap = null;

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            // see comments above about update: dec 2020
            String msg = "Unable to retrieve ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get()";
            logger.info(msg);
            // throw new IllegalArgumentException(msg);
            return null;
        }

        paramMap = new HttpRequestParameterMap(req.getHttpRequest().getParameterMap());
        kvp = req.getRawKvp();
        PairsWMSQueryParam result = new PairsWMSQueryParam(paramMap);
        return result;
    }

    private ImageDescriptor buildRequestImageDescriptor() {
        String bboxStr = paramMap.get("BBOX");
        StringTokenizer bboxTkn = new StringTokenizer(bboxStr, ",");
        double swlat = Double.parseDouble(bboxTkn.nextToken());
        double swlon = Double.parseDouble(bboxTkn.nextToken());
        double nelat = Double.parseDouble(bboxTkn.nextToken());
        double nelon = Double.parseDouble(bboxTkn.nextToken());
        int height = Integer.parseInt((String) paramMap.get("HEIGHT"));
        int width = Integer.parseInt((String) paramMap.get("WIDTH"));
        BoundingBox bbox = new BoundingBox(swlon, swlat, nelon, nelat);

        return new ImageDescriptor(bbox, height, width);
    }

    public List<PairsRasterRequest> generateRequestForEachLayer() {
        List<PairsRasterRequest> result = new ArrayList<>();
        ImageDescriptor requestedImageDescriptor = requestImageDescriptor;

        for (PairsLayerRequestType plrt : layers) {
            PairsRasterRequest rasterRequest = new PairsRasterRequest(plrt, requestedImageDescriptor);
            result.add(rasterRequest);
        }

        return result;
    }

    public Map<String, String> validateParams(Map<String, String[]> params) {
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

    public String getIbmpairslayer() {
        return this.ibmpairslayer;
    }

    public void setIbmpairslayer(String ibmpairslayer) {
        this.ibmpairslayer = ibmpairslayer;
    }
}
