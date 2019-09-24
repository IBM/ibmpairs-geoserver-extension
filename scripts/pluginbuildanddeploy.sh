#!/bin/bash

HOST="pairs-alpha"
HOST="test-pairs-dev01"
TOMCAT_FOLDER="tomcat-geoserver2"
TOMCAT_FOLDER="tomcat-geoserver"
HELP="$ buildanddeploy HOST TOMCAT_FOLDER"
DEFAULT_ARGS="example: $ buildanddeploy $HOST $TOMCAT_FOLDER"

if [ $# -ge 1 ] && ([ "$1" == "-h" ] || [ "$1" = "--help" ]); then
    echo $HELP
    echo $DEFAULT_ARGS
    exit 0
fi    

if [ $# -ge 1 ]; then
    HOST=$1
fi

if [ $# -ge 2 ]; then
    TOMCAT_FOLDER=$2
fi

TARGET_PATH="/pairs_data/www/$TOMCAT_FOLDER/webapps/geoserver/WEB-INF/lib/"

DEPLOY_URL="hduser@$HOST:$TARGET_PATH"
echo "deploying pairs geoserver extension to $DEPLOY_URL"

mvn clean package -DskipTests=true
scp target/pairs-hbase-plugin-0.9.jar $DEPLOY_URL