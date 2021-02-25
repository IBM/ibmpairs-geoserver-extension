package com.ibm.pa.pairs.geoserver.plugin.coveragestore;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.util.factory.Hints;

/**
 * See https://docs.geotools.org/latest/userguide/welcome/use.html
 */
public class CoverageStoreTest {
    static final Param PAIRS_DATASTORE_URL_PARAM = new Param("ibmpairs", String.class, "unique id for pairs layer",
            true, "ibmpairs://pairs.ibm.com/layer1");

    public static void main(String[] args) throws IOException {
        showFormats();
    }

    public static void showFormats() {
        Map<String, Serializable> params = new HashMap<>();
        String url = "http://ibmpairs.ibm.com";
        GridFormatFactorySpi formatFactory;
        AbstractGridFormat format = null;
        params.put(PAIRS_DATASTORE_URL_PARAM.key, "ibmpairs:");

        Hints hints = null;

        // Method1
        Set<GridFormatFactorySpi> formatFactories = GridFormatFinder.getAvailableFormats();

        for (GridFormatFactorySpi factory : formatFactories) {
            System.out.println(
                    "Factory: " + factory.toString() + ", Hints: " + factory.getImplementationHints().toString());
            format = factory.createFormat();
            System.out.println("\tFormat; name: " + format.getName() + ", desc: " + format.getDescription());
            // if(format.accepts( url, hints))
            // break;
        }

        // Method 2 use higher level functions that do some of work of method1
        // Set<AbstractGridFormat> formats = GridFormatFinder.findFormats(url, hints);
        // // Same as above loop
        // format = GridFormatFinder.findFormat(url);
        // GridCoverage2DReader gridReader = format.getReader(url, hints);
    }
}