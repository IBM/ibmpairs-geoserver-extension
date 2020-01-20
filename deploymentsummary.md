## dev and test deployment summary for geoserver, hbase-data-service

### test-pairs-dev01

* pairs extension (and hbase-data-service) config file (persistence.xml) at location ${catalina.home}/etc (persistence.xml in /etc/META-INF)

tomcat folder        webapp folder name    war size     version      port    pairs ext     ext built with geotools
tomcat-geoserver       geoserver            108MB        2.16.0      8082      0.21.0       20.0
tomcat-geoserver02     geoserver2-14-0      92MB         2.14.0      8084      0.21.0       20.0

### pairs-alpha

nginx maps location /geoserver02/ {
192             proxy_pass http://localhost:8082/geoserver/; 

and the test client goes to var geoserverWmsTestUrl = 'https://pairs-alpha.res.ibm.com:8080/geoserver02/pairs/wms';

tomcat-geoserver2       geoserver            92MB         2.14.0      8082      0.21.0                20.0
tomcat-geoserver3       geoserver            108MB        2.16.0      8084      0.21.1                22.0 

