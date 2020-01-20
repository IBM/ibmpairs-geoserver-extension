# dev and test deployment summary for geoserver, hbase-data-service

## test-pairs-dev01

Note config file locations for pairs extension and hbase-data-service and persistence.xml at location ${catalina.home}/etc (persistence.xml in /etc/META-INF)

``` bash
tomcat folder        webapp folder name    war size     version      port    pairs ext     ext built with geotools
tomcat-geoserver-dev      geoserver         108MB        2.16.0      8084      0.22.1       22
tomcat-geoserver02     geoserver2-14-0      92MB         2.14.0      8084      0.2       20.0
```

Hbase-data-service backends are at:

``` bash
/home/hduser/nb/hbase-data-service  scripts/startservice.sh 9084
```

Pairs geoserver plugins map to the hbase-data-service backend found in their config json file in the tomcat/etc folder

``` bash
tomcat-geoserver-dev     "pairsDataServiceBaseRasterUrl" : "http://localhost:9084/api/v1/dataquery/raster"
```

test client served by apache2 at `/var/www/html/datapreview/` 

## pairs-alpha

Geoservers for testing

``` bash
tomcat-geoserver-stable       geoserver         108MB        2.16.0      8082      0.21.0                22.0
tomcat-geoserver-dev       geoserver            108MB        2.16.0      8084      0.22.1                22.0 
```

nginx mappings for geoservers

``` bash
196         location /geoserver_stable/ {
197             proxy_pass http://localhost:8082/geoserver/;
200         location /geoserver_dev/ {
201             proxy_pass http://localhost:8084/geoserver/;
```

Pairs geoserver plugins map to the hbase-data-service backend found in their config json file in the tomcat/etc folder

``` bash
tomcat-geosserver-stable "pairsDataServiceBaseRasterUrl" : "http://localhost:9082/api/v1/dataquery/raster"
tomcat-geoserver-dev     "pairsDataServiceBaseRasterUrl" : "http://localhost:9084/api/v1/dataquery/raster"
```

Hbase-data-service backends are at:

``` bash
tomcat-hbase-data-service-stable  9082
tomcat-hbase-data-service-dev     9084
/home/hduser/nb/hbase-data-service  scripts/startservice.sh 9084
```

nginx mapping for hbase data service above

``` bash
         location  /api/v1/dataquery {
108                 proxy_pass http://localhost:9084/api/v1/dataquery;
111         location  /api/v1_stable/dataquery {
112                 proxy_pass http://localhost:9082/api/v1/dataquery;
115         location  /api/v1_dev/dataquery {
116                 proxy_pass http://localhost:9084/api/v1/dataquery;
```

