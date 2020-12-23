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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.util.factory.Hints;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 * Provides basic information about the Pairs format IO. This is currently an
 * extension of the Geotools AbstractGridFormat because the stream and file GCEs
 * will pick it up if it extends AbstractGridFormat.
 *
 * @author Bryce Nordgren, USDA Forest Service
 * @author Simone Giannecchini
 * @source $URL$
 */
public class PairsFormat extends AbstractGridFormat {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(PairsFormat.class);
    public static final String COVERAGE_NAME = "IBMPairs (Pairs Raster)";
    private static final String PAIRS_SCHEME = "ibmpairs";

    /**
     * Creates a new instance of PairsFormat from call to
     * PairsFormatFactory.createFormat()
     * 
     * 
     * 
     */
    public PairsFormat() {
        writeParameters = null;
        mInfo = new HashMap<String, String>();
        mInfo.put("name", COVERAGE_NAME);
        mInfo.put("description", "Pairs HBase connection");
        mInfo.put("vendor", "IBM Pairs");
        mInfo.put("version", "0.9");
        mInfo.put("docURL", "http://www.ibmpairs.mybluemix.net");

        /**
         * 
         * TODO: test, Norm added OVER_VIEW.. DECIMATION, .. READ_GRIDGEO is required I
         * think
         */
        // reading parameters
        readParameters = new ParameterGroup(
                new DefaultParameterDescriptorGroup(mInfo, new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D,
                        INPUT_TRANSPARENT_COLOR, SUGGESTED_TILE_SIZE }));

        /*
         * GeneralParameterDescriptor[] parameterDescriptors = new
         * GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D, OVERVIEW_POLICY,
         * DECIMATION_POLICY }; DefaultParameterDescriptorGroup defaultParameterGroup =
         * new DefaultParameterDescriptorGroup(mInfo, parameterDescriptors);
         * readParameters = new ParameterGroup(defaultParameterGroup);
         */
        writeParameters = null;
    }

    public boolean accepts(Object source) {
        return accepts(source, null);
        // return accepts(source, GeoTools.getDefaultHints());
    }

    /**
     * @param source the source object to test for compatibility with this format.
     */
    @Override
    public boolean accepts(Object source, Hints hints) {
        boolean result = false;
        if (source == null) {
            result = false;
        } else if (source instanceof String) {
            String val = (String) source;
            if (val.startsWith(PAIRS_SCHEME))
                result = true;
        }
        return result;
    }

    @Override
    public PairsCoverageReader getReader(Object source) {
        return getReader(source, null);
    }

    /**
     *
     */
    @Override
    public PairsCoverageReader getReader(Object source, Hints hints) {
        PairsCoverageReader result = null;
        LOGGER.log(Level.WARNING, "PairsExtension (geoserverlogger) Source: " + source.toString());
        if (source instanceof String) {
            try {
                result = new PairsCoverageReader(source, hints);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "PairsFormat::()getReader: " + e.getMessage());
            }
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