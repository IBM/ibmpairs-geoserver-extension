mvn exec:java -Dexec.mainClass=com.ibm.pa.pairs.geoserver.plugin.hbase.PairsUtilities \ 
    -Djvm.args="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y"