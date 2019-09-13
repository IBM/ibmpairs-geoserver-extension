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

// Geotools dependencies

import java.awt.RenderingHints;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

/**
 * The <CODE>PairsFormatFactorySpi</CODE> should never be instantiated directly
 * by user code. It is discovered by the <CODE>GridFormatFinder</CODE> for
 * automatic discovery. Use the standard Geotools method of discovering a
 * factory in order to create a format.
 *
 * <p>
 * This format will only report itself to be &quot;available&quot; if the JAI
 * and JAI ImageI/O libraries are available. Otherwise it will be unavailable.
 * If a user attempts to create a new instance of the format when the required
 * libraries are unavailable, an <CODE>
 * UnsupportedOperationException</CODE> will be thrown.
 *
 * @author Bryce Nordgren / USDA Forest Service
 * @author Simone Giannecchini
 * @source $URL$
 */
public class PairsFormatFactory implements GridFormatFactorySpi {
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(PairsFormatFactory.class);

    public static void main(String[] args) {
    }

    /**
     * TODO: not sure this test is necessary
     */
    // public final static String myClassName = "com.ibm.pa.pairs.geoserver.plugin.hbase.PairsFormatFactory";
    static {
        String myClassName = PairsFormatFactory.class.getName();
        try {
            Class.forName(myClassName);
        } catch (Exception e) {
            e.printStackTrace();
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, "Unable to load" + myClassName, e);
        }
    }

    public PairsFormatFactory() {
    }

    /**
     * When Geoserver UI 'datastores' is invoked, the createFormat() method of all
     * registered implementers of GridFormatFactorySpi are called. (This will be in
     * the 'raster' section of the UI list which splits datastores into vector,
     * raster). The AbstractGridFormat( PairsFormat) returned from createFormat()
     * fills in the minfo structure to provide info about the datastore displayed in
     * the UI.
     * 
     * When a new datastore instance is created, we enter a URL which must be valid
     * in the field. ( I believe we could also enter a valid filename, see below) By
     * doing so, an org.geotools.data.DataStore is created, the geotools default
     * implmentation of datastore for a URL is created. (I don't know exactly which
     * class this is yet.) To add a unique url scheme like ibmpairs:// we would add
     * to the underlying java URL as per note from Fernando Mino in response to my post:
     * 
     * http://osgeo-org.1560.x6.nabble.com/Geoserver-extension-to-use-custom-schema-when-crateing-new-data-store-td5392397.html
     * 
     *  - Since Java URL
     * class support only few well known protocols by default (http, file, jar...)
     * you could need implement a custom handler for your new URL protocol in first
     * place. Good old BalusC explains how to create one in this link:
     * https://stackoverflow.com/a/26409796/3662679 - 
     * Alternatively we could registor our own DataStore by implementing org.geotools.data.DataStoreFactorySpi,
     * register in META-INF. See for example public class ShapefileDataStoreFactory implements FileDataStoreFactorySpi {...}
     * Then we add Params as shown in that class to express the valide parsing of the string entered in the 
     * Geoserver UI when creating a new DataStore.
     * 
     * 
     * Creates and returns a new instance of the <CODE>PairsPairsFormat</CODE> class
     * if the required libraries are present. If JAI and JAI Image I/O are not
     * present, will throw an <CODE>
     * 
     * 
     * UnsupportedOperationException</CODE>.
     *
     * @return <CODE>PairsFormat</CODE> object.
     * @throws UnsupportedOperationException if this format is unavailable.
     */
    public AbstractGridFormat createFormat() {
        if (!isAvailable()) {
            throw new UnsupportedOperationException("The Pairs plugin requires the JAI and JAI ImageI/O libraries!");
        }

        return new PairsFormat();
    }

    /**
     * Informs the caller whether the libraries required by the Pairs reader are
     * installed or not.
     *
     * @return availability of the Pairs format.
     */
    public boolean isAvailable() {
        boolean available = true;

        // if these classes are here, then the runtine environment has
        // access to JAI and the JAI ImageI/O toolbox.
        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
        } catch (ClassNotFoundException cnf) {
            available = false;
        }

        return available;
    }

    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }
}
