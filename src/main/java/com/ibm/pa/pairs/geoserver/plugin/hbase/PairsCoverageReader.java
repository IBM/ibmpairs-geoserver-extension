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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.image.io.ImageIOExt;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
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

/**
 * **************************************
 * 
 * Note regarding originalEnvelope and originalGridRange:
 * 
 * These refer to the BBOX (lon, lat) and grid (x,y pixels) of the data source.
 * These objects do not seem to be used in significant way in the normal WMS
 * raster image serving. For normal serving, the Coverage object returned by the
 * read() method here tells Geoserver the exact BBOX and pixel range of the
 * returned raster.
 * 
 * For requests like WPS we find that Geoserver queries the GeneralEnvelope and
 * GridRange prior to issuing the call to read(). The dimensions in these
 * objects are used by Geoserver to scale and (affine)transform the Params it
 * passes to read(). Therefore, the original envelope and GridRange contents
 * must be very accurate. The values must correspond to a BBOX and pixel size of
 * the returned Pairs image in the 4326 CRS. It seems the main parameter
 * Geoserver requires is the pixel size in degrees/pixel. Geoserver determines
 * the scale by fitting an affine transform to the corners of the boxes returned
 * in the envelope and gridRange. If the data source was a geotiff file, this
 * information is available from the geotiff metadata which is directly read. In
 * the GeoTiffReader extension the Envelope and grid range are populated by the
 * actual dimensions of the geotiff source file. (This metadata can be displayed
 * by, opening the goetiff in QGis. It would show that a sentinel 2 high res
 * image geotiff created by query to the pairs client has metadata indicating
 * its scale in X,Y are both 64uDeg/pixe. These would be exactly ratio of the
 * size of the file in lon, lat divided by the number of pixels in X or Y
 * respectively)
 * 
 * For the Pairs extension, a query must be made to pairs hbase data service to
 * get the pairs pixel scale that the image will be returned in. For Sentinel 2,
 * the Pairs pixel esolution for the layer (23 which is == 2^(29-23)uDeg). Then,
 * given that resolution we create an originalEnvelope and originalgridRange
 * that have exactly that dimension ratio in lon,lat and x,y pixel size. Also,
 * we make the overall dimension of the Envelope to cover the input range in the
 * http request parameters. If the image will be returned from an overview, the
 * scale of the overview is returned by the call to the hbase data service.
 * Overviews could also be handle by setting a rasterModel in
 * AbstractGridCoverage2DReader, but hope we don't have to add that
 * complication.
 * 
 * TODO: There are a couple things that need more understanding here regarding
 * the input CRS. The native CRS for PAIRS plugin layer is 4326. That is how
 * data stored in hbase. WHen a WMS req comes in with this CRS4326, Geoserver
 * has no issue and builds these parameters which it logs to catalina.out. There
 * is no projection (PROJCS) because I believe for 4326 just maps lon,lat
 * directly out to flat pixel array.
 * 
 * Crs = GEOGCS["WGS 84", DATUM["World Geodetic System 1984",
 * 
 * When a request comes in for crs 3857 we find a slight difference in that
 * there is a projection. But the request is handled correctly, just as for
 * 4326.
 * 
 * Crs = PROJCS["WGS 84 / Pseudo-Mercator", GEOGCS["WGS 84", DATUM["World
 * Geodetic System 1984",
 * 
 * 
 * HOWEVER, the problem arises when we try to do a WPS gs:CropCoverage operation
 * and the input layer is 3857. The originalEnvelope setting is now used
 * extensively in the Goetools code path. It works fine if the original
 * GeneralEnvelope is in 4326, but when its in 3857 we fail. In
 * getPairsOriginalEnvelope() I've tried to set the CRS to the input 3857, but
 * that still causes errors, usually a WARN coordinates seem to exceed allowed
 * range. Snd it breaks even a normal WMS getMap() without cropping. SO, this
 * needs more work to make Cropping work with CRS other than 4326.
 * 
 * I recently found some very useful static utilities in geotools Coveridge class to convert coverages
 * between CRS. So can try to covert 4326 to 3857 when we return it from read(..).
 * 
 * 
 * End Note regarding originalEnvelope and originalGridRange:
 * *********************************************
 */
public class PairsCoverageReader extends AbstractGridCoverage2DReader {
    public static final Logger logger = Logger.getLogger(PairsCoverageReader.class.getName());
    public static int GRID_WIDTH = 512;
    public static int GRID_HEIGHT = 256;
    PairsWMSQueryParam httpRequestParams;
    double pairsPixelResolution = -1;

    /*
     * 
     * @throws FactoryException
     * 
     * @throws NoSuchAuthorityCodeException
     * 
     */
    public PairsCoverageReader(Object input, Hints uHints) throws NoSuchAuthorityCodeException, FactoryException,
            ClientProtocolException, URISyntaxException, IOException {
        super(input, uHints);
        coverageName = input.toString();
        crs = CRS.decode("EPSG:4326");

        httpRequestParams = PairsWMSQueryParam.getRequestQueryStringParameter();
        if (httpRequestParams != null) {
            pairsPixelResolution = getPairsPixelResolution();
            originalEnvelope = getPairsOriginalEnvelope();
            originalGridRange = getPairsOriginalGridRange();
            setlayout(new ImageLayout(0, 0, getOriginalGridRange().getSpan(0), getOriginalGridRange().getSpan(1)));
        } else {
            originalEnvelope = getMyOriginalEnvelope(); // temporary fix
            originalGridRange = getMyOriginalGridRange(); // temporary fix
            setlayout(new ImageLayout(0, 0, getMyOriginalGridRange().getSpan(0), getMyOriginalGridRange().getSpan(1)));
            // setlayout(new ImageLayout(0, 0, GRID_WIDTH, GRID_HEIGHT));
        }

        coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(hints);
        if (coverageFactory == null) {
            logger.log(Level.WARNING, "Pairs; Couldn't find exsiting coverageFactory for hints, creating new");
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
    // @Override
    // public GeneralEnvelope getOriginalEnvelope() {
    // return this.getOriginalEnvelope(coverageName);
    // }

    // @Override
    // public GeneralEnvelope getOriginalEnvelope(String coverageName) {
    // return this.originalEnvelope;
    // }

    // @Override
    // public GridEnvelope getOriginalGridRange() {
    // return this.getOriginalGridRange(coverageName);
    // }

    // @Override
    // public GridEnvelope getOriginalGridRange(String coverageName) {
    // return this.originalGridRange;
    // }

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

    private double getPairsPixelResolution() throws URISyntaxException, ClientProtocolException, IOException {
        // return 0.000064;
        ImageDescriptor imageDescriptor = httpRequestParams.getRequestImageDescriptor();
        int layerId = this.httpRequestParams.getLayerid();
        String statistic = this.httpRequestParams.getStatistic();
        return PairsUtilities.getPairsResolution(layerId, statistic, imageDescriptor);
    }

    /**
     * 
     * @return
     * @throws NoSuchAuthorityCodeException
     * @throws FactoryException
     */
    GeneralEnvelope getPairsOriginalEnvelope() throws NoSuchAuthorityCodeException, FactoryException {
        GeneralEnvelope result = null;

        double lonSpan = httpRequestParams.getRequestImageDescriptor().getBoundingBox().getWidth();
        double latSpan = httpRequestParams.getRequestImageDescriptor().getBoundingBox().getHeight();
        int xSpan = (int) (lonSpan / this.pairsPixelResolution) + 1;
        int ySpan = (int) (latSpan / this.pairsPixelResolution) + 1;
        lonSpan = xSpan * this.pairsPixelResolution;
        latSpan = ySpan * this.pairsPixelResolution;
        double[] swLonLat = httpRequestParams.getRequestImageDescriptor().getBoundingBox().getSwLonLat();
        double[] nelonLat = { swLonLat[0] + lonSpan, swLonLat[1] + latSpan };

        result = new GeneralEnvelope(swLonLat, nelonLat);
        // result.setCoordinateReferenceSystem(CRS.decode(httpRequestParams.getCrs()));

        return result;
    }

    GridEnvelope getPairsOriginalGridRange() {
        GeneralGridEnvelope result = null;

        double lonSpan = httpRequestParams.getRequestImageDescriptor().getBoundingBox().getWidth();
        double latSpan = httpRequestParams.getRequestImageDescriptor().getBoundingBox().getHeight();
        int xSpan = (int) (lonSpan / this.pairsPixelResolution) + 1;
        int ySpan = (int) (latSpan / this.pairsPixelResolution) + 1;

        result = new GeneralGridEnvelope(new int[] { 0, 0 }, new int[] { xSpan, ySpan });
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
        Rectangle dim = null;
        Color inputTransparentColor = null;
        OverviewPolicy overviewPolicy = null;
        int[] suggestedTileSize = null;

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
            // orig
            // boundingBox = new BoundingBox(-180, -85, 180, 85);
            // requestImageDescriptor = new ImageDescriptor(boundingBox, 384, 768);

            // Testing code oct 2019
            logger.info("Geoserver invoked special case; dim.height, width == 5 case");
            boundingBox = new BoundingBox(-10, -10, 10, 10);
            requestImageDescriptor = new ImageDescriptor(boundingBox, 5, 5);
            float[] mockupImage = new float[requestImageDescriptor.height * requestImageDescriptor.width];
            result = buildGridCoverage2D(requestImageDescriptor, mockupImage);
            return result;
        }

        Rectangle2D rectangle2D = requestedEnvelope.toRectangle2D();
        double swlon = rectangle2D.getMinX();
        double swlat = rectangle2D.getMinY();
        double nelon = rectangle2D.getMaxX();
        double nelat = rectangle2D.getMaxY();
        boundingBox = new BoundingBox(swlon, swlat, nelon, nelat);
        requestImageDescriptor = new ImageDescriptor(boundingBox, dim.height, dim.width);
        /**
         * TODO: An image descriptor is built inside PairsWMSQueryParams
         * (httpRequestParams) and should be same as the one in the local vars above.
         * This should be confirmed, and if so then use the one in PairsWMSQueryParams
         * and remove local var. Then, the line below should be: URI uri =
         * PairsUtilities.buildPairsDataServiceRasterRequestUri(httpRequestParams);
         */
        PairsWMSQueryParam pairsWMSQueryParams = PairsWMSQueryParam.getRequestQueryStringParameter();
        logger.info("Local Request ImageDescriptor: " + requestImageDescriptor.toString());
        logger.info("httpRequestParams Request ImageDescriptor: "
                + pairsWMSQueryParams.getRequestImageDescriptor().toString());

        try {
            HttpResponse response = PairsUtilities.getRasterFromPairsDataService(pairsWMSQueryParams,
                    requestImageDescriptor);

            String pairsHeaderJson = PairsUtilities.getResponseHeader(response,
                    PairsGeoserverExtensionConfig.PAIRS_HEADER_KEY);
            ImageDescriptor responseImageDescriptor = PairsUtilities.deserializeJson(pairsHeaderJson,
                    ImageDescriptor.class);

            logger.info("Response ImageDescriptor: " + responseImageDescriptor.toString());

            byte[] rawData = PairsUtilities.readRawContent(response);
            float[] imageDataFloat = PairsUtilities.byteArray2FloatArray(rawData);

            result = buildGridCoverage2D(responseImageDescriptor, imageDataFloat);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
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

        if (PairsGeoserverExtensionConfig.getInstance().getCreateCoverage2DMethod()
                .equals(PairsGeoserverExtensionConfig.RASTER)) {
            float[][] raster = PairsUtilities.vector2array(imageVector, responseImageDescriptor.getWidth());
            result = coverageFactory.create(coverageName, raster, responseEnvelope);
        } else if (PairsGeoserverExtensionConfig.getInstance().getCreateCoverage2DMethod()
                .equals(PairsGeoserverExtensionConfig.BUFFERED_IMAGE)) {
            BufferedImage image = getImage(responseImageDescriptor, imageVector, "default");
            result = coverageFactory.create(coverageName, image, responseEnvelope);
        } else {
            logger.log(Level.WARNING, "Unknown coverage generation type: "
                    + PairsGeoserverExtensionConfig.getInstance().getCreateCoverage2DMethod());
        }

        return result;
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
            logger.log(Level.SEVERE, "Error getting image: " + e.getMessage());
            result = PairsUtilities.getTestImageIntRGB(imageDescriptor.getWidth(), imageDescriptor.getHeight());
        }
        return result;
    }

    /**
     * This is a place to set custom IBM Pairs response headers on the WMS getMap
     * reply Note: for the geoserver 'test' in coverage reader where
     * dim.height=dim.width = 5 this won't provide an org.geoserver.ows.Request req
     * = org.geoserver.ows.Dispatcher.REQUEST.get() as I don't think there is truly
     * a WMS request.
     */
    private HttpServletResponse setPairsWMSHttpResponse() {
        org.geoserver.ows.Request req = org.geoserver.ows.Dispatcher.REQUEST.get();
        if (req != null) {
            HttpServletResponse response = req.getHttpResponse();
            response.setHeader(PairsGeoserverExtensionConfig.PAIRS_HEADER_KEY, "norm is here");
            return response;
        } else {
            logger.log(Level.WARNING,
                    "Unable to retrieve HttpServletResponse on geoserver thread-local; Pairs response header not set");
            return null;
        }
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
