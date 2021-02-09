package com.ibm.pa.pairs.geoserver.plugin.datastore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

public class PairsDataStoreTest {

    public static void main() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put(PairsDataStoreFactory.PAIRS_DATASTORE_PARAM.key, "ibmpairs://pairs.ibm.com/layer1");
        DataStore store = DataStoreFinder.getDataStore(params);

        store.dispose();
    }
}