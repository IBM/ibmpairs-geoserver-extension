package com.ibm.pa.pairs.geoserver.plugin.hbase;

public class PairsRasterRequest {
    private final PairsLayerRequestType pairsLayer;
    private final ImageDescriptor requestImageDescriptor;
    private String statistic = "";
    private String ibmpairsquery;

    public PairsRasterRequest(PairsLayerRequestType pairsLayer, ImageDescriptor requestedImageDescriptor) {
        this.pairsLayer = pairsLayer;
        this.requestImageDescriptor = requestedImageDescriptor;
        this.ibmpairsquery = pairsLayer.toJson();
    }

    public PairsLayerRequestType getPairsLayer() {
        return this.pairsLayer;
    }

    public ImageDescriptor getRequestImageDescriptor() {
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
