package com.ibm.pa.pairs.geoserver.plugin.hbase;

public class PairsRasterRequest {
    private final PairsLayerRequestType pairsLayer;
    private final ImageDescriptor requestedImageDescriptor;
    private String statistic = "";
    private String ibmpairsquery;

    public PairsRasterRequest(PairsLayerRequestType pairsLayer, ImageDescriptor requestedImageDescriptor) {
        this.pairsLayer = pairsLayer;
        this.requestedImageDescriptor = requestedImageDescriptor;
        this.ibmpairsquery = pairsLayer.toJson();
    }

    public PairsLayerRequestType getPairsLayer() {
        return this.pairsLayer;
    }

    public ImageDescriptor getRequestedImageDescriptor() {
        return this.requestedImageDescriptor;
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
