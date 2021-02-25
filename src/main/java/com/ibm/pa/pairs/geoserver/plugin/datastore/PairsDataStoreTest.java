package com.ibm.pa.pairs.geoserver.plugin.datastore;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataAccessFactory.Param;
import org.vfny.geoserver.util.DataStoreUtils;
import org.geotools.data.DataStoreFinder;

/**
 * See https://docs.geotools.org/latest/userguide/welcome/use.html
 */
public class PairsDataStoreTest {

    public static void main(String[] args) throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        PairsDataStore pairsDataStore = null;
        params.put(PairsDataStoreFactory.PAIRS_DATASTORE_URL_PARAM.key, "ibmpairs:");

        // Method 1 to get datastore, bad practice, uses specific factory
        PairsDataStoreFactory factory = new PairsDataStoreFactory();
        // pairsDataStore = (PairsDataStore) factory.createDataStore( params );

        List<String> descriptions = DataStoreUtils.listDataStoresDescriptions();
        for( String s : descriptions) {
            System.out.println("DatastoreDescription: + " + s);
        }


        // Method 2 to get datastore, better practice use DataStoreFinder
        pairsDataStore = (PairsDataStore) DataStoreFinder.getDataStore(params);
        boolean canProcess = pairsDataStore.getDataStoreFactory().canProcess(params);

        pairsDataStore.dispose();
    }
}