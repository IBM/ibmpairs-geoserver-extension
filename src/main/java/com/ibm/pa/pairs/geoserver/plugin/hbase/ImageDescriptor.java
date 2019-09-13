package com.ibm.pa.pairs.geoserver.plugin.hbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Encapsulates information return to client as JSON in the X-PAIRS-DATA header
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageDescriptor {
    int height;
    int width;
    BoundingBox boundingBox;

    public ImageDescriptor(BoundingBox boundingBox, int height, int width) {
        this.boundingBox = boundingBox;
        this.height = height;
        this.width = width;
    }

    public ImageDescriptor(double[] boundingBox, int height, int width) {
        this.boundingBox = new BoundingBox(boundingBox);
        this.height = height;
        this.width = width;
    }

    public ImageDescriptor() {
    }

    /**
     * New ImageDescriptor with contents the difference[this - tgt]
     */
    public ImageDescriptor difference(ImageDescriptor tgt) {
        ImageDescriptor result = new ImageDescriptor( boundingBox.difference(tgt.getBoundingBox()),
        getHeight() - tgt.getHeight(),
        getWidth() - tgt.getWidth());
        return result;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public ImageDescriptor height(int height) {
        this.height = height;
        return this;
    }

    public ImageDescriptor width(int width) {
        this.width = width;
        return this;
    }

    public ImageDescriptor boundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    public String toString() {
        String result = String.format("Bounding box: %s, Height: %d, Width: %d", boundingBox.toString(), height, width);
        return result;
    }
}