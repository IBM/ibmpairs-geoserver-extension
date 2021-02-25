/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package com.ibm.pa.pairs.geoserver.plugin.datastore;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

/**
 * DataAccessFactory for working with formats based on a single URL.
 *
 * <p>
 * This interface provides a mechanism of discovery for DataAccessFactories
 * which support singular files.
 *
 * @author dzwiers
 * @source $URL$
 */
public class PairsDataStoreFactory implements DataStoreFactorySpi {
    private final static Logger logger = org.geotools.util.logging.Logging.getLogger(PairsDataStoreFactory.class);
    public static final Param PAIRS_DATASTORE_URL_PARAM = new Param("url", String.class, "unique id for pairs data store",
            true, "http://pairs.ibm.com/datastore1");

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        for(String key : params.keySet()) {
            Object value = params.get(key);
            logger.info( "canProcess(): key: " + key + ", value: " + value);
        }

        boolean chk = DataStoreFactorySpi.super.canProcess(params);
        return chk;
    }

    /**
     * The typeName represented by the provided url.
     *
     * @param url The location of the datum to parse into features
     * @return Returns the typename of the datum specified (on occasion this may
     *         involve starting the parse as well to get the FeatureType -- may not
     *         be instantanious).
     * @throws IOException
     */
    public String getTypeName(URL url) throws IOException {
        return "PairsDataStoreFactory:TypeName url:" + url;
    }

    @Override
    public String getDisplayName() {
        return "PairsDataStoreFactory:DisplayName IBM Pairs";
    }

    @Override
    public String getDescription() {
        return "PairsDataStoreFactory:Description IBM PAIRS vector data";
    }

    @Override
    public Param[] getParametersInfo() {
        return new Param[] { PAIRS_DATASTORE_URL_PARAM };
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        PairsDataStore dataStore = new PairsDataStore(params);
        dataStore.setDataStoreFactory(this);
        return dataStore;
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        return createDataStore(params);
    }
}
