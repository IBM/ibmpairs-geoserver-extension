# dev and test deployment summary for geoserver, hbase-data-service

## test-pairs-dev01

Note config file locations for pairs extension and hbase-data-service and persistence.xml at location ${catalina.home}/etc (persistence.xml in /etc/META-INF)

```
tomcat folder        webapp folder name    war size     version      port    pairs ext     ext built with geotools
tomcat-geoserver       geoserver            108MB        2.16.0      8082      0.21.0       20.0
tomcat-geoserver02     geoserver2-14-0      92MB         2.14.0      8084      0.21.0       20.0
```

## pairs-alpha
```
 location /geoserver_stable/ {
197             proxy_pass http://localhost:8082/geoserver/;
198         }
199 
200         location /geoserver_dev/ {
201             proxy_pass http://localhost:8084/geoserver/;
```

and the test client goes to var geoserverWmsTestUrl = 'https://pairs-alpha.res.ibm.com:8080/geoserver02/pairs/wms';

tomcat-geoserver2       geoserver            92MB         2.14.0      8082      0.21.0                20.0
tomcat-geoserver3       geoserver            108MB        2.16.0      8084      0.21.1                22.0 

Geoserver pairs plugins map to the hbase-data-service backend found in their config json file in the tomcat/etc folder