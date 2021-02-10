package com.ibm.pa.pairs.geoserver.plugin.datastore;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStoreFinder;

public class PairsDataStoreTest {
    static final Param PAIRS_DATASTORE_URL_PARAM = new Param("ibmpairs", String.class, "unique id for pairs layer",
    true, "ibmpairs://pairs.ibm.com/layer1");

    public static void main(String[] args) throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put(PAIRS_DATASTORE_URL_PARAM.key, "ibmpairs:");

        PairsDataStore store = (PairsDataStore) DataStoreFinder.getDataStore(params);
        boolean canProcess = store.getDataStoreFactory().canProcess(params);

        store.dispose();
    }
}