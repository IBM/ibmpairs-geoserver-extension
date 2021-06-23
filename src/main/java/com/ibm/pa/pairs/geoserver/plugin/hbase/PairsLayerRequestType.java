package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
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
        this.dimensions = dimensions;
        // validateDimensions();
    }

    /**
     * 
     * Validate the dimension names and order provided (e.g. 'horizon') against the
     * database. Translates to the PAIRS internal name for that dimension.
     * 
     * todo: validate the value as well as the name, and check that the order filed
     * matches. Dimensions must be provided in same order as given in the database
     * for that dimension
     * 
     * @param dimensions
     * @return
     * @throws Exception
     */
    /**
     * public void validateDimensions() throws Exception { if (dimensions == null)
     * return;
     * 
     * int requestOrder = 1; for (Dimension dim : dimensions) { DimensionProperties
     * dp = PairsCoreDAO.getDimensionProperties(id, dim.name); dim.identifier =
     * dp.identifier; dim.order = dp.order; if (requestOrder != dim.order) { String
     * cause = String.format( "Dimension request out of order; Layer: %s, Dimension:
     * %s, requestOrder: %d, internalOrder: %d", id, dim.name, requestOrder,
     * dim.order); throw new Exception(cause); } } }
     */

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
     * "10"}]}]}
     * 
     * [{"id": "49523", "temporal": "1262304000"}, {"id": "49524", "temporal":
     * "1262304000"}]
     * 
     * When json starts with '[' its an array of layers and deserializes against
     * PairsLayerRequestType[].class
     * 
     * When json starts with '{' can be either a single layer {"id": "1", ....}
     * which deserializes against PairsLayerRequetType.clas; Or, a layer stanza like
     * {"layers"; [{"id": "1", ....}, {"id": "2", ....}]} which will deserialize
     * with the pairsLayerWrapper that has the "layers" tag
     * 
     * @param json
     * @return
     * @throws Exception
     */
    public static PairsLayerRequestType[] buildFromJson(String json) throws Exception {
        PairsLayerRequestType[] result = null;
        List<String> excepts = new ArrayList<>();

        if (result == null) {
            try {
                result = JsonSerializable.deserializeStatic(json, PairsLayerRequestType[].class);
            } catch (Exception e) {
                excepts.add("Case: layer array [{},{}...]: " + e.getMessage());
            }
        }

        if (result == null) {
            try {
                PairsLayerWrapper pairsLayerWrapper = JsonSerializable.deserializeStatic(json, PairsLayerWrapper.class);
                result = pairsLayerWrapper.layers;
            } catch (Exception e) {
                excepts.add("Case: layer array wrapped {\"layers\": [{id, temporal,..},{id,..}]}: " + e.getMessage());
            }
        }

        if (result == null) {
            try {

                PairsLayerRequestType pairsLayer = JsonSerializable.deserializeStatic(json,
                        PairsLayerRequestType.class);
                result = new PairsLayerRequestType[] { pairsLayer };
            } catch (Exception e) {
                excepts.add("Case: single layer {id, temporal, ..}: " + e.getMessage());
            }
        }

        if (result == null) {
            String all = "All json parse errors:";
            for (String m : excepts)
                all += "\n\tMSG: " + m;
            throw new Exception(all);
        }

        // for (PairsLayerRequestType rlrt : result) {
        // rlrt.validateDimensions();
        // rlrt.validatePixelLevel();
        // }

        return result;
    }

    public static void main(String[] args) {
        String json = testSerialize();
        testDeserializeJson(json);
    }

    public static void testDeserializeJson(String json) {
        String layer1nodim_json = "{\"id\":51,\"temporal\":{\"intervals\":[{\"snapshot\":\"1573603200\",\"end\":null}]},\"dimensions\":null,\"pixelLevel\":-1}";
        String layer1dim1_json = "{\"id\":51,\"temporal\":{\"intervals\":[{\"snapshot\":\"1573603200\",\"start\":null,\"end\":null}]},\"dimensions\":[{\"name\":\"horizon\",\"value\":\"30\",\"identifier\":null,\"order\":null}],\"pixelLevel\":-1}";
        String layer2dim1_json = "[" + layer1dim1_json + "," + layer1dim1_json + "]";
        String wrapped_json = "{\"layers\": " + layer2dim1_json + "}";
        // String json4 = \"{\\"layers\\":[{\\"id\\": \\"49523\\", \\"temporal\\":
        // \"1262304000\", \"dimension\":[{\"name\":\"horizon\",
        // \"value\":\"30\"},{\"name\": \"elevation\", \"value\": \"10\"}]}]}";

        try {
            System.out.println("\tinput json: " + wrapped_json + "\n");
            PairsLayerRequestType[] result = PairsLayerRequestType.buildFromJson(wrapped_json);
            System.out.println("Success!!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static String testSerialize() {
        String json = null;
        try {
            // PairsLayerRequestType me = new PairsLayerRequestType(51, "1573603200", -1);
            // PairsLayerRequestType me = new PairsLayerRequestType(51, "1573603200", -1,
            // "horizon", "30");
            PairsLayerRequestType me = new PairsLayerRequestType(51, "1573603200", -1,
                    new Dimension[] { new Dimension("horizon", "30"), new Dimension("elevation", "40") });

            json = me.toJson();
            System.out.println("\njson: \n\t" + json);
        } catch (Exception e) {
            System.out.println("Failed: " + e.getMessage());
        }
        return json;
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

    /*******************************
     * Utility classes
     ********************************/

    public static class PairsLayerWrapper {
        public PairsLayerRequestType[] layers;
    }

    public static class Temporal {
        public Interval[] intervals;

        public Temporal() {
        }

        public Temporal(String snapshot) {
            Interval interval = new Interval();
            interval.snapshot = snapshot;
            this.intervals = new Interval[] { interval };
        }

        public Interval[] getIntervals() {
            return intervals;
        }

        public void setIntervals(Interval[] intervals) {
            this.intervals = intervals;
        }
    }

    public static class Interval {
        public String snapshot;
        public String start;
        public String end;

        public Interval() {
        }
    }

    public static class Dimension {
        public String name;
        public String value;
        public String identifier;
        public Integer order;

        public Dimension() {
        }

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
