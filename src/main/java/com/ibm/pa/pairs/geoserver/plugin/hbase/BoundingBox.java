package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Bounding box in lat long degrees Stored as [swlon, swlat, nelon, nelat] TODO:
 * check limits in constructor and position in -180,180, -90, 90
 */
public class BoundingBox {
    private double[] boundingArray;

    public BoundingBox(String bbox) {
        boundingArray = new double[4];
        String[] coords = bbox.split(",");
        boundingArray[0] = Double.parseDouble(coords[0]);
        boundingArray[1] = Double.parseDouble(coords[1]);
        boundingArray[2] = Double.parseDouble(coords[2]);
        boundingArray[3] = Double.parseDouble(coords[3]);
    }

    public BoundingBox(double swlon, double swlat, double nelon, double nelat) {
        this(new double[] { swlon, swlat, nelon, nelat });
    }

    public BoundingBox(final double[] swLonLat, final double[] neLonLat) {
        this(new double[] { swLonLat[0], swLonLat[1], neLonLat[0], neLonLat[1] });
    }

    public BoundingBox(final double[] boundingArray) {
        this.boundingArray = boundingArray;
    }

    private BoundingBox() {
        boundingArray = new double[4];
    }

    /**
     * Return this - tgt at each location
     */
    public BoundingBox difference(BoundingBox tgt) {
        double[] diff = new double[4];
        for (int i = 0; i < boundingArray.length; i++) {
            diff[i] = boundingArray[i] - tgt.getBoundingArray()[i];
        }
        return new BoundingBox(diff);
    }

    public double[] getBoundingArray() {
        return boundingArray;
    }

    @JsonIgnore
    public double[] getSwLonLat() {
        return new double[] { boundingArray[0], boundingArray[1] };
    }

    @JsonIgnore
    public double[] getNeLonLat() {
        return new double[] { boundingArray[2], boundingArray[3] };
    }

    @JsonIgnore
    public double getHeight() {
        return getNeLonLat()[1] - getSwLonLat()[1];
    }

    @JsonIgnore
    public double getWidth() {
        return getNeLonLat()[0] - getSwLonLat()[0];
    }

    public String toQueryParam() {
        String format = "%f,%f,%f,%f";
        String str = String.format(format, boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3]);
        return str;
    }

    public String toString() {
        String format = "SW(lon,lat): (%f, %f), NE(lon,lat): (%f, %f)";
        String str = String.format(format, boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3]);
        return str;
    }

    // TODO: improve, currently somewhat fixes bounds, doesn't adjust. Should it
    // throw
    // errors?
    public static boolean checkDimensions(double[] bbox) {
        boolean result = true;
        if (bbox[0] < -180)
            bbox[0] = -180;
        if (bbox[1] < -90)
            bbox[1] = -90;
        if (bbox[2] > 180)
            bbox[2] = 180;
        if (bbox[3] > 90)
            bbox[3] = 90;

        return result;
    }

    public static void main(String[] args) {
        BoundingBox bb = new BoundingBox(new double[] { -181, -90 }, new double[] { 180, 95 });
        Arrays.toString(bb.getBoundingArray());
        BoundingBox.checkDimensions(bb.getBoundingArray());
        Arrays.toString(bb.getBoundingArray());
    }

}