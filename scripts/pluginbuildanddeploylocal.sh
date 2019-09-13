#!/bin/bash

mvn clean package -DskipTests=true
cp target/pairs-hbase-plugin-0.9.jar /usr/local/opt/www/tomcat-geoserver/webapps/geoserver/WEB-INF/lib/