/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2015, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given.
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.image.io.ImageIOExt;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.ReferenceIdentifier;

/**
 * this class is responsible for exposing the data and the Georeferencing
 * metadata available to the Geotools library. This reader is heavily based on
 * the capabilities provided by the ImageIO tools and JAI libraries.
 *
 * @author Bryce Nordgren, USDA Forest Service
 * @author Simone Giannecchini
 * @since 2.1
 */
public class PairsCoverageReader extends AbstractGridCoverage2DReader implements GridCoverage2DReader {
    public static int GRID_WIDTH = 512;
    public static int GRID_HEIGHT = 256;
    public static final Logger logger = Logger.getLogger(PairsCoverageReader.class.getName());

    /**
     * TODO: get original envelope, we should keep this in class that encapsulates
     * the creation of this datastore.
     * 
     * @throws FactoryException
     * @throws NoSuchAuthorityCodeException
     * 
     */
    public PairsCoverageReader(Object input, Hints uHints)
            throws DataSourceException, NoSuchAuthorityCodeException, FactoryException {
        super(input, uHints);
        // update super class state
        // coveragename should be name of store parsed from string
        coverageName = (String) source;
        crs = CRS.decode("EPSG:4326");
        originalEnvelope = getMyOriginalEnvelope(); // temporary fix
        originalGridRange = getMyOriginalGridRange(); // temporary fix

        super.setlayout(new ImageLayout(0, 0, GRID_WIDTH, GRID_HEIGHT));
        coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(hints);
        if (coverageFactory == null) {
            logger.severe("Pairs; Couldn't find exsiting coverageFactory for hints, creating new");
            coverageFactory = new GridCoverageFactory();
        }
    }

    /**
     * NOTE: These overrides differ from parent class implementation in that they
     * return the 'original' class instance. The parent methods create new instances
     * using a 'copy' constructor. e.g. return GeneralEnvelope ge = new
     * GeneralEnvelope(this.orignalEnvelope)
     * 
     * Not sure what is the correct to do. Geomesa overrides as is done here
     */
    @Override
    public GeneralEnvelope getOriginalEnvelope() {
        return this.originalEnvelope;
    }

    @Override
    public GeneralEnvelope getOriginalEnvelope(String coverageName) {
        return this.originalEnvelope;
    }

    @Override
    public GridEnvelope getOriginalGridRange() {
        return this.originalGridRange;
    }

    @Override
    public GridEnvelope getOriginalGridRange(String coverageName) {
        return this.originalGridRange;
    }

    // @Override
    // public CoordinateReferenceSystem getCoordinateReferenceSystem() {
    // return crs;
    // }

    /** @see org.opengis.coverage.grid.GridCoverageReader#getFormat() */
    @Override
    public Format getFormat() {
        return new PairsFormat();
    }

    /**
     * Number of coverages for this reader is 1
     *
     * @return the number of coverages for this reader.
     */
    @Override
    public int getGridCoverageCount() {
        return 1;
    }

    GeneralEnvelope getMyOriginalEnvelope() throws NoSuchAuthorityCodeException, FactoryException {
        GeneralEnvelope result = null;
        double minlon = -180, minlat = -90;
        double maxlon = 180, maxlat = 90;
        double[] minBB = new double[] { minlon, minlat }, maxBB = new double[] { maxlon, maxlat };
        result = new GeneralEnvelope(minBB, maxBB);
        result.setCoordinateReferenceSystem(this.getCoordinateReferenceSystem());
        result.setCoordinateReferenceSystem(crs);
        return result;
    }

    GridEnvelope getMyOriginalGridRange() {
        GeneralGridEnvelope result = null;
        result = new GeneralGridEnvelope(new int[] { 0, 0 }, new int[] { GRID_WIDTH, GRID_HEIGHT });
        return result;
    }

    /**
     *
     * @param params currently ignored, potentially may be used for hints.
     * @return grid coverage represented by the image
     * @throws IOException on any IO related troubles
     */
    @Override
    public GridCoverage2D read(GeneralParameterValue[] params) throws IOException {
        GridCoverage2D result = null;
        GeneralEnvelope requestedEnvelope = null;
        Envelope envelope2dOrig = null;
        Rectangle dim = null;
        Color inputTransparentColor = null;
        OverviewPolicy overviewPolicy = null;
        int[] suggestedTileSize = null;

        envelope2dOrig = getOriginalEnvelope();
        ImageIO io;
        ImageIOExt ioe;

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                final ParameterValue param = (ParameterValue) params[i];
                final ReferenceIdentifier name = param.getDescriptor().getName();
                logger.info("Parameter: " + i + ", overview string: " + params[i].toString() + ", Descriptor: "
                        + name.toString());

                if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName())) {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    requestedEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());
                    dim = gg.getGridRange2D().getBounds();
                    continue;
                }
                if (name.equals(AbstractGridFormat.OVERVIEW_POLICY.getName())) {
                    overviewPolicy = (OverviewPolicy) param.getValue();
                    continue;
                }
                if (name.equals(AbstractGridFormat.INPUT_TRANSPARENT_COLOR.getName())) {
                    inputTransparentColor = (Color) param.getValue();
                    continue;
                }
                if (name.equals(AbstractGridFormat.SUGGESTED_TILE_SIZE.getName())) {
                    String suggestedTileSize_ = (String) param.getValue();
                    if (suggestedTileSize_ != null && suggestedTileSize_.length() > 0) {
                        suggestedTileSize_ = suggestedTileSize_.trim();
                        int commaPosition = suggestedTileSize_.indexOf(",");
                        if (commaPosition < 0) {
                            int tileDim = Integer.parseInt(suggestedTileSize_);
                            suggestedTileSize = new int[] { tileDim, tileDim };
                        } else {
                            int tileW = Integer.parseInt(suggestedTileSize_.substring(0, commaPosition));
                            int tileH = Integer.parseInt(suggestedTileSize_.substring(commaPosition + 1));
                            suggestedTileSize = new int[] { tileW, tileH };
                        }
                    }
                    continue;
                }
            }
        }

        /**
         * TODO: Understands hints and relation to other processing factory plugins
         * Figure out if newHints should be added to super.hints If hints suggest a
         * processing factory they should be invoked or its a bug
         */
        Hints newHints = null;
        if (suggestedTileSize != null) {
            newHints = hints.clone();
            final ImageLayout layout = new ImageLayout();
            layout.setTileGridXOffset(0);
            layout.setTileGridYOffset(0);
            layout.setTileHeight(suggestedTileSize[1]);
            layout.setTileWidth(suggestedTileSize[0]);
            newHints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
        }

        BoundingBox boundingBox = null;
        ImageDescriptor requestImageDescriptor = null;

        // if (params.width.toInt == 5 && params.height.toInt == 5) {
        // TODO: https://geomesa.atlassian.net/browse/GEOMESA-868,
        // https://geomesa.atlassian.net/browse/GEOMESA-869
        // logger.warning(
        // "Suspected GeoServer registration layer, returning a default image for now
        // until mosaicing fixed");
        // result = coverageFactory.create(coverageName,
        // RasterUtils.defaultBufferedImage, params.envelope);
        // }
        if (dim.height == 5 && dim.width == 5) {
            boundingBox = new BoundingBox(-180, -85, 180, 85);
            requestImageDescriptor = new ImageDescriptor(boundingBox, 384, 768);
        } else {
            Rectangle2D rectangle2D = requestedEnvelope.toRectangle2D();
            double swlon = rectangle2D.getMinX();
            double swlat = rectangle2D.getMinY();
            double nelon = rectangle2D.getMaxX();
            double nelat = rectangle2D.getMaxY();
            boundingBox = new BoundingBox(swlon, swlat, nelon, nelat);
            requestImageDescriptor = new ImageDescriptor(boundingBox, dim.height, dim.width);
        }

        PairsWMSQueryParam pairsParams = PairsWMSQueryParam.getRequestQueryStringParameter();
        logger.info("Request ImageDescriptor: " + requestImageDescriptor.toString());

        try {
            URI uri = PairsUtilities.buildPairsDataServiceUri(pairsParams.getLayerid(), pairsParams.getTimestamp(), -1,
                    pairsParams.getStatistic(), requestImageDescriptor);

            HttpResponse response = PairsUtilities.getHttpResponse(uri);
            String pairsHeaderJson = PairsUtilities.getResponseHeader(response, PairsGeoserverExtensionConfig.PAIRS_HEADER_KEY);
            ImageDescriptor responseImageDescriptor = PairsUtilities.deserializeJson(pairsHeaderJson,
                    ImageDescriptor.class);

            logger.info("Response ImageDescriptor: " + responseImageDescriptor.toString());

            byte[] rawData = PairsUtilities.readRawContent(response);
            float[] imageDataFloat = PairsUtilities.byteArray2FloatArray(rawData);

            result = buildGridCoverage2D(responseImageDescriptor, imageDataFloat);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new IOException(e.getMessage());
        }

        setPairsWMSHttpResponse();
        return result;
    }

    private GridCoverage2D buildGridCoverage2D(ImageDescriptor responseImageDescriptor, float[] imageVector) {
        GridCoverage2D result = null;

        Envelope2D responseEnvelope = new Envelope2D(getCoordinateReferenceSystem(),
                responseImageDescriptor.getBoundingBox().getSwLonLat()[0],
                responseImageDescriptor.getBoundingBox().getSwLonLat()[1],
                responseImageDescriptor.getBoundingBox().getWidth(),
                responseImageDescriptor.getBoundingBox().getHeight());

        // diagnostic only remove later
        // float[] minmax = PairsUtilities.getMinMax(imageVector);

        if (PairsGeoserverExtensionConfig.getInstance().getCreateCoverage2DMethod().equals(PairsGeoserverExtensionConfig.RASTER)) {
            float[][] raster = PairsUtilities.vector2array(imageVector, responseImageDescriptor.getWidth());
            result = coverageFactory.create(coverageName, raster, responseEnvelope);
        } else if (PairsGeoserverExtensionConfig.getInstance().getCreateCoverage2DMethod().equals(PairsGeoserverExtensionConfig.BUFFERED_IMAGE)) {
            BufferedImage image = getImage(responseImageDescriptor, imageVector, "default");
            result = coverageFactory.create(coverageName, image, responseEnvelope);
        } else {
            logger.warning(
                    "Unknown coverage generation type: " + PairsGeoserverExtensionConfig.getInstance().getCreateCoverage2DMethod());
        }

        return result;
    }

    private BufferedImage getImage(ImageDescriptor imageDescriptor, float[] data, String method) {
        BufferedImage result = null;

        switch (method) {
        case "getGrayImageFromIntData":
            int[] intData = PairsUtilities.floatArray2ScaledIntArray(data);
            result = getGrayImageFromIntData(imageDescriptor, intData);
            break;

        case "default":
        case "getGrayImageFromFloatData":
            result = getGrayImageFromFloatData(imageDescriptor, data);
            break;

        case "getRGBImageFromFloatData":
            result = getRGBImageFromFloatData(imageDescriptor, data);
            break;

        default:
            throw new IllegalArgumentException("Invalid image create method: " + method);
        }

        return result;
    }

    private BufferedImage getGrayImageFromIntData(ImageDescriptor imageDescriptor, int[] data) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        int[] nBits = { 8 };
        ColorModel cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT);
        SampleModel sm = cm.createCompatibleSampleModel(imageDescriptor.getWidth(), imageDescriptor.getHeight());
        DataBufferInt db = new DataBufferInt(data, imageDescriptor.getWidth() * imageDescriptor.getHeight());
        WritableRaster raster = Raster.createWritableRaster(sm, db, null);
        BufferedImage image = new BufferedImage(cm, raster, false, null);

        return image;
    }

    /**
     * Similar to getImageGray_BYTE, but uses float raster directly, needs test
     */
    private BufferedImage getGrayImageFromFloatData(ImageDescriptor imageDescriptor, float[] data) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        int[] nBits = { 8 };
        ColorModel cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
        SampleModel sm = cm.createCompatibleSampleModel(imageDescriptor.getWidth(), imageDescriptor.getHeight());
        DataBufferFloat db = new DataBufferFloat(data, imageDescriptor.getWidth() * imageDescriptor.getHeight());
        WritableRaster raster = Raster.createWritableRaster(sm, db, null);
        BufferedImage image = new BufferedImage(cm, raster, false, null);

        return image;
    }

    private BufferedImage getRGBImageFromFloatData(ImageDescriptor imageDescriptor, float[] data) {
        BufferedImage image = new BufferedImage(imageDescriptor.getWidth(), imageDescriptor.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        WritableRaster r = image.getRaster();
        r.setPixels(0, 0, imageDescriptor.getWidth(), imageDescriptor.getHeight(), data);
        return image;
    }

    /**
     * urlstr =
     * "http://pairs-web04:9082/api/v2/data?layerid=PairsUtilities.TEST_LAYERID&timestamp=PairsUtilities.TEST_TIMESTAMP&swlat=30.0&swlon=-80.0&nelat=40.712&nelon=-70.0060&height=128&width=256";
     */
    private BufferedImage getImageRGB1(ImageDescriptor imageDescriptor, float[] data) {
        BufferedImage result = new BufferedImage(imageDescriptor.getWidth(), imageDescriptor.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        int[] imageDataInt = PairsUtilities.floatArray2IntArray(data);
        WritableRaster writeableRaster = result.getRaster();
        writeableRaster.setDataElements(0, 0, result.getWidth(), result.getHeight(), imageDataInt);

        try {
            result.setRGB(0, 0, imageDescriptor.getWidth(), imageDescriptor.getHeight(), imageDataInt, 0,
                    imageDescriptor.getWidth());
        } catch (Exception e) {
            logger.severe("Error getting image: " + e.getMessage());
            result = PairsUtilities.getTestImageIntRGB(imageDescriptor.getWidth(), imageDescriptor.getHeight());
        }
        return result;
    }

    /**
     * Set custom IBM Pairs response headers on the WMS getMap reply
     */
    private HttpServletResponse setPairsWMSHttpResponse() {
        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        HttpServletResponse response = req.getHttpResponse();
        response.setHeader(PairsGeoserverExtensionConfig.PAIRS_HEADER_KEY, "norm is here");
        return response;
    }

    /**
     * Crop and resample to requested NOTE; The input image is shallow copy and
     * return so note that any changes to result pixels will reflect in original
     * 
     * @param img
     * @param src
     * @param tgt
     * @return
     */
    private BufferedImage cropImage(BufferedImage img, ImageDescriptor src, ImageDescriptor tgt) {
        double degreeToPixelX = src.getBoundingBox().getWidth() / src.getWidth(); // X,Y should be ==
        double degreeToPixelY = src.getBoundingBox().getHeight() / src.getHeight();
        double degreeToPixel = degreeToPixelX;

        int offsetX = (int) Math
                .round((tgt.getBoundingBox().getSwLonLat()[0] - src.getBoundingBox().getSwLonLat()[0]) / degreeToPixel);
        int offsetY = (int) Math
                .round((tgt.getBoundingBox().getSwLonLat()[1] - src.getBoundingBox().getSwLonLat()[1]) / degreeToPixel);
        int width = (int) Math.round(src.getBoundingBox().getWidth() / degreeToPixel);
        int height = (int) Math.round(src.getBoundingBox().getHeight() / degreeToPixel);

        BufferedImage dest = img.getSubimage(offsetX, offsetY, width, height);
        return dest;
    }

    /**
     * see https://memorynotfound.com/java-resize-image-fixed-width-height-example/
     * Add imageObserver to drawImage(...) call if want to wait in loop after
     * drawImage() call until image drawn, this probably not required, async
     * rendering OK
     */
    private BufferedImage resizeImage(BufferedImage img, ImageDescriptor tgt) {
        ImageObserver imageObserver = null;

        Image rescaled = img.getScaledInstance(tgt.getWidth(), tgt.getHeight(), Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(tgt.getWidth(), tgt.getHeight(), img.getType());
        resized.getGraphics().drawImage(rescaled, 0, 0, imageObserver);
        resized.getGraphics().dispose();
        return resized;
    }

    /**
     * Use in above to make resizeImage() synchronous, not tested
     */
    ImageObserver imageObserver = new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            if ((infoflags & ImageObserver.ALLBITS) != ALLBITS)
                return true;
            else
                return false;
        }
    };

}

/**
 * Graveyard final byte[] a = ((DataBufferByte)
 * result.getRaster().getDataBuffer()).getData(); System.arraycopy(rgbArray, 0,
 * a, 0, h * w);
 * 
 * Rescale and resize to match requested boundingBox and screen pixel grid
 * Currently leave to Geoserver to do this using returned GridCoverage2D Its a
 * little complex, image has to be resampled to higher resolution, then cropped
 * in a way that both target bbox and grid sizes are met.
 * 
 * image = resizeImage(image, requestImageDescriptor); image = cropImage(image,
 * responseImageDescriptor, requestImageDescriptor);
 */
