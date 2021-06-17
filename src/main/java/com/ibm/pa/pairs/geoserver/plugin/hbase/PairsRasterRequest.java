package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.ArrayList;
import java.util.List;

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

    public static List<PairsRasterRequest> generateRequestForEachLayer(PairsWMSQueryParam queryParam) {
        List<PairsRasterRequest> result = new ArrayList<>();
        ImageDescriptor requestedImageDescriptor = queryParam.getRequestImageDescriptor();

        for (PairsLayerRequestType plrt : queryParam.getLayers()) {
            PairsRasterRequest rasterRequest = new PairsRasterRequest(plrt, requestedImageDescriptor);
            result.add(rasterRequest);
        }

        return result;
    }
}
