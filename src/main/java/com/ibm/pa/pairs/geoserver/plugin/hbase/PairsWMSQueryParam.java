package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ibm.pa.utils.JsonSerializable;
import com.ibm.pa.utils.PairsHttpRequestParamMap;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.CRS.AxisOrder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
    PairsHttpRequestParamMap paramMap;
    String statistic;
    Integer level = -1;
    String crs;
    String ibmpairslayer;
    PairsLayerRequestType[] layers;
    PairsImageDescriptor pairsImageDescriptor;

    public PairsWMSQueryParam(PairsImageDescriptor pairsImageDescriptor, final PairsHttpRequestParamMap paramMap)
            throws Exception {
        Map<String, String> invalidParams;
        this.paramMap = paramMap;
        this.pairsImageDescriptor = pairsImageDescriptor;

        invalidParams = validateParams(paramMap);
        if (!invalidParams.isEmpty())
            throw new IllegalArgumentException(invalidParams.toString());

        setStatistic(paramMap.get(PairsGeoserverExtensionConfig.PAIRS_QUERY_KEY_STATISTIC, "mean"));
        setCrs(paramMap.get("CRS", "EPSG:4326"));
        setIbmpairslayer(paramMap.get(PairsGeoserverExtensionConfig.PAIRS_LAYER_JSON));
        layers = PairsLayerRequestType.buildFromJson(ibmpairslayer);
    }

    /**
     * Used on PairsCoverageReader.read(...) mehod to build params to use in call to
     * pairsdataservice API to get the raster from hbase
     * 
     * @return
     * @throws Exception
     */
    public static PairsWMSQueryParam buildPairsWMSQueryParamFromCoverageRequest(GeneralEnvelope requestEnvelope,
            Rectangle requestGridDimensions) throws Exception {
        Map<String, Object> kvp = null;
        PairsHttpRequestParamMap paramMap = null;

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            // see comments above about update: dec 2020
            String msg = "Unable to retrieve ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get()";
            logger.info(msg);
            // throw new IllegalArgumentException(msg);
            return null;
        }

        paramMap = new PairsHttpRequestParamMap(req.getHttpRequest().getParameterMap());
        kvp = req.getRawKvp();

        PairsImageDescriptor pairsImageDescriptor = buildRequestImageDescriptor(requestEnvelope, requestGridDimensions);
        PairsWMSQueryParam result = new PairsWMSQueryParam(pairsImageDescriptor, paramMap);

        return result;
    }

    private static PairsImageDescriptor buildRequestImageDescriptor(GeneralEnvelope requestEnvelope,
            Rectangle requestGridDimensions) {
        double swlonlat[] = requestEnvelope.getLowerCorner().getCoordinate();
        double nelonlat[] = requestEnvelope.getLowerCorner().getCoordinate();
        BoundingBox bbox = new BoundingBox(swlonlat, nelonlat);
        int height = (int) requestGridDimensions.getBounds().getHeight();
        int width = (int) requestGridDimensions.getBounds().getHeight();

        return new PairsImageDescriptor(bbox, height, width);
    }

    /**
     * Used in PairsCoverageReader constructor to help get originalEnvelope etc
     * which has to be done from http query params since that is all info avail at
     * time of call.
     * 
     * @return
     * @throws Exception
     */
    public static PairsWMSQueryParam buildPairsWMSQueryParamFromQueryParams() throws Exception {
        Map<String, Object> kvp = null;
        PairsHttpRequestParamMap paramMap = null;

        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req == null) {
            // see comments above about update: dec 2020
            String msg = "Unable to retrieve ThreadLocal org.geoserver.ows.Dispatcher.REQUEST.get()";
            logger.info(msg);
            // throw new IllegalArgumentException(msg);
            return null;
        }

        paramMap = new PairsHttpRequestParamMap(req.getHttpRequest().getParameterMap());
        kvp = req.getRawKvp();

        PairsImageDescriptor pairsImageDescriptor = buildRequestImageDescriptor(paramMap);
        PairsWMSQueryParam result = new PairsWMSQueryParam(pairsImageDescriptor, paramMap);

        return result;
    }

    /**
     * Note, EPSG:4326 is (lat,lon) order in EPSG database. So must be specified
     * that way in the bbox param on WMS getMap request to Gesoerver for WMS 1.3+.
     * (see https://docs.geoserver.org/stable/en/user/services/wms/basics.html)
     * Example;
     * geoserver/wms?VERSION=1.1.1&REQUEST=GetMap&SRS=epsg:4326&BBOX=-180,-90,180,90&…
     * geoserver/wms?VERSION=1.3.0&REQUEST=GetMap&CRS=epsg:4326&BBOX=-90,-180,90,180&…
     * 
     * 
     * @param paramMap
     * @return
     * @throws FactoryException
     * @throws NoSuchAuthorityCodeException
     */
    private static PairsImageDescriptor buildRequestImageDescriptor(PairsHttpRequestParamMap paramMap)
            throws NoSuchAuthorityCodeException, FactoryException {
        final String bboxQueryParam = paramMap.get("BBOX");
        String bboxCorrected = null;
        String WMSVersion = paramMap.get("VERSION");
        String[] versionComp = WMSVersion.split(".");
        boolean is13plus = Integer.parseInt(versionComp[1]) >= 3;
        String crsStr = paramMap.get("CRS");
        CoordinateReferenceSystem cref = CRS.decode(crsStr);
        AxisOrder axisOrder = CRS.getAxisOrder(cref);

        if (is13plus && axisOrder == AxisOrder.NORTH_EAST) {
            String[] bboxComp = bboxQueryParam.split(",");
            String selon = bboxComp[1];
            String selat = bboxComp[0];
            String nelon = bboxComp[3];
            String nelat = bboxComp[2];
            bboxCorrected = String.join(",", selon, selat, nelon, nelat);
        } else
            bboxCorrected = bboxQueryParam;

        BoundingBox bbox = new BoundingBox(bboxCorrected);
        int height = Integer.parseInt((String) paramMap.get("HEIGHT"));
        int width = Integer.parseInt((String) paramMap.get("WIDTH"));

        return new PairsImageDescriptor(bbox, height, width);
    }

    public List<PairsRasterRequest> generateRequestForEachLayer() {
        List<PairsRasterRequest> result = new ArrayList<>();
        PairsImageDescriptor requestedImageDescriptor = pairsImageDescriptor;

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

    public PairsLayerRequestType getLayer(int i) {
        return this.layers[i];
    }

    public String getStatistic() {
        return this.statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public PairsImageDescriptor getRequestImageDescriptor() {
        return pairsImageDescriptor;
    }

    public void setRequestImageDescriptor(PairsImageDescriptor requestImageDescriptor) {
        this.pairsImageDescriptor = requestImageDescriptor;
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
