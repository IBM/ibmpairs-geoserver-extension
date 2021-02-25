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
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.type.Name;

/**
 * DataStore represents a single file of content.
 *
 * <p>
 * Allows developer to skip refering to the typeName when a file contains only a
 * single set of content.
 *
 * @source $URL$
 */
public class PairsDataStore extends ContentDataStore {
        Map<String, Serializable> params;

        public PairsDataStore(Map<String, Serializable> params) {
                super();
                this.params = params;
                DefaultServiceInfo defaultServiceInfo = (DefaultServiceInfo) super.getInfo();
                defaultServiceInfo.setTitle("IBM Pairs data store info");
                defaultServiceInfo.setDescription("Data store for Pairs virtual layers");
        }

        @Override
        protected List<Name> createTypeNames() throws IOException {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
                // TODO Auto-generated method stub
                return null;
        }
}
