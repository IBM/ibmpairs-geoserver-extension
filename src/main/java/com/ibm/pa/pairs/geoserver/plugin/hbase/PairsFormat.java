/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.utils.URIBuilder;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * Provides basic information about the Pairs format IO. An instance is created
 * from the PairsFormatFactory which is found during the factory scan of the SPI
 * ServiceRegistry.
 * 
 * Example code see geotools implementation of Geotiff plugin. Geotools github
 * repo and navigate to geotiff plugin at
 * modules/plugin/geotiff/src/...../GeoTiffFormat.java (pkg
 * org.geotools.gce.geotiff)
 * 
 * The 'source' object should be a URL, either http or file protocol. It is
 * specified during creation through the Geoserver UI 'add data store' dialog,
 * or via the Geoserver REST API. A quick validity test is done is done at
 * creation by Geoserver. For http, an openConnection must respond with a stream
 * (as of Geoserver 2.18 the stream is not read) so the endpoint must be valid.
 * for file:// the file must exist.
 * 
 * NOTE: File based connections, can be local to each machine. For a virtual
 * layer the file just has to exist, it can be empty. If needed, the file can
 * include some metadata for each pairs plugin virtual layer. Http based
 * connections can be global so the connection shared for all Geoservers
 * 
 * TODO: For better support of http connections, add http GET endpoint in pairs
 * hbase data service listening on V2/pairsplugin/* The Geoserver connection url
 * would be http://pairs.res.ibm.com/pairsdataservice/v2/pairsplugin/conn1,
 * ..conn2, ... These endpoints could be in turn backed by files. And could be
 * read to support extra metadata.
 * 
 * @author Bryce Nordgren, USDA Forest Service
 * @author Simone Giannecchini
 * @source $URL$
 */
public class PairsFormat extends AbstractGridFormat {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(PairsFormat.class);
    public static final String COVERAGE_NAME = "IBMPairs (Pairs Raster)";
    private static final String[] PAIRS_SIGNATURE = { "ibm", "pairs", "pairsplugin" };

    /**
     * Creates a new instance of PairsFormat from call to
     * PairsFormatFactory.createFormat()
     * 
     * TODO: Add OVER_VIEW.. DECIMATION, .. READ_GRIDGEO is required I think
     * parameters here should be passed as Hints? Add some PAIRS custom PARAMS and
     * test
     */
    public PairsFormat() {
        mInfo = new HashMap<String, String>();
        mInfo.put("name", COVERAGE_NAME);
        mInfo.put("description", "Live Pairs HBase data connection");
        mInfo.put("vendor", "IBM Pairs");
        mInfo.put("version", "0.9.1");
        mInfo.put("docURL", "http://www.ibmpairs.mybluemix.net");

        super.readParameters = new ParameterGroup(
                new DefaultParameterDescriptorGroup(mInfo, new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D,
                        INPUT_TRANSPARENT_COLOR, SUGGESTED_TILE_SIZE }));

        /*
         * GeneralParameterDescriptor[] parameterDescriptors = new
         * GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D, OVERVIEW_POLICY,
         * DECIMATION_POLICY }; DefaultParameterDescriptorGroup defaultParameterGroup =
         * new DefaultParameterDescriptorGroup(mInfo, parameterDescriptors);
         * readParameters = new ParameterGroup(defaultParameterGroup);
         */
        super.writeParameters = null;
    }

    public boolean accepts(Object source) {
        return accepts(source, null);
        // return accepts(source, GeoTools.getDefaultHints());
    }

    /**
     * @param source - Object to test for compatibility with this format.
     */
    @Override
    public boolean accepts(Object source, Hints hints) {
        boolean result = isValidSource(source, hints);
        return result;
    }

    @Override
    public PairsCoverageReader getReader(Object source) {
        return getReader(source, null);
    }

    @Override
    public PairsCoverageReader getReader(Object source, Hints hints) {
        PairsCoverageReader result = null;

        if (isValidSource(source, hints)) {
            try {
                result = new PairsCoverageReader(source, hints);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "PairsFormat::getReader(): " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Validate the source. Source is the URI from the connection field in the
     * Create Data Store menu of the Geoserver UI. For a pairs coverage store it is
     * either an http uri or a FILE. The http URL MUST be valid in the sense that an
     * openConnection returns an input stream. (See geoserver class
     * FileExistsValidator.java which validates the UI Form). For http scheme (e.g.
     * http://www.ibm.com) we are passed the entire URI as a String Type (e.g.
     * http://www.ibm.com). For source instanceOf Java File Type
     * (file:///Users/bobroff/projects/pairs/data/pairspluginfile.tif) we are passed
     * path of the file
     * (/Users/bobroff/projects/pairs/data/pairspluginfile.ibmplugin), i.e.
     * uri.getPath().
     * 
     * Validation on data source creation at the Geoserver UI is done by
     * FileExistsValidator.java
     * 
     * TODO enforce file extension match something like 'pairslayer'
     * 
     * TODO: For proper validation we should open and read the file or URL for valid
     * metadata.
     * 
     * @param source
     * @param hints
     * @return
     */
    public boolean isValidSource(Object source, Hints hints) {
        boolean result = false;
        LOGGER.log(Level.WARNING, "PairsFormat Source class: " + source.getClass().getName() + ", value: " + source);

        if (source instanceof String) { // HTTP scheme comes in as a String type rather than URI
            String urlPath = source.toString();
            for (String sigUri : PAIRS_SIGNATURE) {
                if (urlPath.toLowerCase().contains(sigUri)) {
                    try {
                        // URIBuilder builder = new URIBuilder(urlPath);
                        // java.net.URI uri = builder.build();
                        // URLConnection connection = uri.toURL().openConnection();
                        // connection.setConnectTimeout(2000);
                        // InputStream is = connection.getInputStream();
                        // is.close();
    
                        result = true;
                        break;
                    } catch (Exception e) {
                        result = false;
                    }
                }
            }
        } else if (source instanceof File) {
            File file = (File) source;
            if (!file.exists())
                return false;

            Path path = file.toPath().toAbsolutePath();
            String fileName = path.toString();

            String fileExt = "";
            int fdot = fileName.lastIndexOf(".");
            if (fdot > -1 && fdot < 1 + fileName.length())
                fileExt = fileName.substring(fdot + 1);

            for (String sigFile : PAIRS_SIGNATURE) {
                if (fileName.toLowerCase().contains(sigFile)) {
                    result = true;
                    break;
                }
            }
        } else {
            result = false;
            LOGGER.log(Level.SEVERE, "Unsupported data source protocol: " + source.getClass());
        }
        return result;
    }

    @Override
    public GridCoverageWriter getWriter(Object destination, Hints hints) {
        return null;
    }

    @Override
    public GridCoverageWriter getWriter(Object destination) {
        return null;
    }

    @Override
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        return null;
    }
}

/**
 * Graveyard readParameters = new ParameterGroup( new
 * DefaultParameterDescriptorGroup( mInfo, new GeneralParameterDescriptor[] {
 * READ_GRIDGEOMETRY2D, }));
 */