package com.ibm.pa.pairs.geoserver.plugin.hbase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DimensionProperties {
    public String id;
    public String identifier;
    public String shortName;
    public Integer order;
}