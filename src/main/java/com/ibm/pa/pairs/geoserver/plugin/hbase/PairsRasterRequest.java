package com.ibm.pa.pairs.geoserver.plugin.hbase;


/**
 * Encapsulate the info required to go to pairsdataservice API to get the raw raster data.
 * This is the pairsLayer (id, timestampd, dimensions) and the ImageDescriptor(bbox, resolution),
 * 
 */
public class PairsRasterRequest {
    private final PairsLayerRequestType pairsLayer;
    private final PairsImageDescriptor requestImageDescriptor;
    private String statistic = "";
    private String ibmpairsquery;

    public PairsRasterRequest(PairsLayerRequestType pairsLayer, PairsImageDescriptor requestedImageDescriptor) {
        this.pairsLayer = pairsLayer;
        this.requestImageDescriptor = requestedImageDescriptor;
        this.ibmpairsquery = pairsLayer.toJson();
    }

    public PairsLayerRequestType getPairsLayer() {
        return this.pairsLayer;
    }

    public PairsImageDescriptor getRequestImageDescriptor() {
        return this.requestImageDescriptor;
    }

    public String getStatistic() {
        return this.statistic;
    }

    public void setStatistic(String statistic) {
        this.statistic = statistic;
    }

    public String getIbmpairsquery() {
        return this.ibmpairsquery;
    }

    public void setIbmpairsquery(String ibmpairsquery) {
        this.ibmpairsquery = ibmpairsquery;
    }

}
