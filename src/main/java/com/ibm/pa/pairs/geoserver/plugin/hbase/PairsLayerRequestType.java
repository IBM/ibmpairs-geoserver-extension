package com.ibm.pa.pairs.geoserver.plugin.hbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ibm.pa.utils.JsonSerializable;
import com.ibm.pa.utils.PairsUtilities;

/**
 * Encapsulate the minimal information needed about each layer in a request. For
 * temporal timestamp input: Only snapshot is supported in request JSON, and the
 * timestamp must be exact
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PairsLayerRequestType implements com.ibm.pa.utils.JsonSerializable {
    Integer id;
    Temporal temporal;
    Dimension[] dimensions;
    Integer pixelLevel;

    public PairsLayerRequestType() {
    }

    public PairsLayerRequestType(Integer layerId, String timestamp, Integer pixelLevel) throws Exception {
        this(layerId, timestamp, pixelLevel, null, null);
    }

    public PairsLayerRequestType(Integer layerId, String timestamp, Integer pixelLevel, String dimensionName,
            String dimensionValue) throws Exception {
        this(layerId, timestamp, pixelLevel,
                new Dimension[] { dimensionName == null ? null : new Dimension(dimensionName, dimensionValue) });
    }

    public PairsLayerRequestType(Integer layerId, String timestamp, Integer pixelLevel, Dimension[] dimensions)
            throws Exception {
        this.id = layerId;
        this.pixelLevel = pixelLevel;
        String ts = PairsUtilities.toEpochSecString(timestamp);
        this.temporal = new Temporal(ts);
    }

    public Integer validatePixelLevel() {
        if (pixelLevel == null)
            pixelLevel = -1;

        return pixelLevel;
    }

    /**
     * Utility to create PairsLayer[] from Json matching Pairs Code API query
     * 
     * Examples for ibmpairslayer request parameter
     * 
     * {"layers":[{"id": "49523", "temporal": "1262304000",
     * "dimension":[{"name":"horizon", "value":"30"},{"name": "elevation", "value":
     * "10"}]]}
     * 
     * [{"id": "49523", "temporal": "1262304000"}, {"id": "49524", "temporal":
     * "1262304000"}]
     * 
     * @param json
     * @return
     * @throws Exception
     */
    public static PairsLayerRequestType[] buildFromJson(String json) throws Exception {
        PairsLayerRequestType[] result = null;

        if (json.startsWith("["))
            result = JsonSerializable.deserialize(json, PairsLayerRequestType[].class);
        else if (json.startsWith("{")) {
            PairsLayerWrapper pairsLayerWrapper = JsonSerializable.deserialize(json, PairsLayerWrapper.class);
            result = pairsLayerWrapper.layers;
        }

        for (PairsLayerRequestType rlrt : result) {
            rlrt.validatePixelLevel();
        }

        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("PairsLayer: ");
        sb.append(String.format("id: %s, timestamp: %s", id, temporal.intervals[0].snapshot));

        if (dimensions != null)
            for (Dimension dim : dimensions)
                sb.append(String.format(", dimension: %s", dim.toString()));

        return sb.toString();
    }

    public String toJson() {
        String result = serialize();
        return result;
    } 

    /**
     * Utility classes
     */

    public static class PairsLayerWrapper {
        public PairsLayerRequestType[] layers;
    }

    public static class Temporal {
        public Interval[] intervals;

        public Temporal(String snapshot) {
            Interval interval = new Interval();
            interval.snapshot = snapshot;
            this.intervals = new PairsLayerRequestType.Interval[] { interval };
        }

        public Interval[] getIntervals() {
            return intervals;
        }
    }

    public static class Interval {
        public String snapshot;
        public String start;
        public String end;
    }

    public static class Dimension {
        public String name;
        public String value;
        public String identifier;
        public Integer order;

        public Dimension(String name, String value) {
            this(name, value, null, null);

        }

        public Dimension(String name, String value, String internalName, Integer order) {
            this.name = name;
            this.value = value;
            this.identifier = internalName;
            this.order = order;
        }

        // public String toString() {
        // return " Name: " + name + "internalName: " + internalName + ", order: " +
        // order + ", value: " + value;
        // }
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Temporal getTemporal() {
        return this.temporal;
    }

    public void setTemporal(Temporal temporal) {
        this.temporal = temporal;
    }

    public Dimension[] getDimensions() {
        return this.dimensions;
    }

    public void setDimensions(Dimension[] dimensions) {
        this.dimensions = dimensions;
    }

    public Integer getPixelLevel() {
        return this.pixelLevel;
    }

    public void setPixelLevel(Integer pixelLevel) {
        this.pixelLevel = pixelLevel;
    }
}
