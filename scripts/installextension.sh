#!/bin/bash

HOST="http://test=pairs=dev01.watson.ibm.com:8082"
WS=pairs
DATASTORE_PATH="workspaces/$WS/datastores"

HELP="install hosturl [-u datastore url] [-n store name]"
PARAMS=""
while (("$#")); do
  case "$1" in
  -h | --help)
    echo $HELP
    exit 1
    ;;
  -H | --host)
    HOST=$2
    shift 2
    ;;
  --) # end argument parsing
    shift
    break
    ;;
  -* | --*=) # unsupported flags
    echo "Error: Unsupported flag $1" >&2
    exit 1
    ;;
  *) # preserve positional arguments
    PARAMS="$PARAMS $1"
    shift
    ;;
  esac
done
# set positional arguments in their proper place
echo params: $PARAMS
eval set -- "$PARAMS"
echo params: $PARAMS

ACCEPTS="accepts: application/json"

BODY_FILE="scripts/pairsdatastore.json"

RESULT=$($CMD)
echo RESULT

# Query all coverage stores
curl -v -u admin:geoserver -H "Content-Type: application/json" http://localhost:9080/geoserver/rest/workspaces/pairs/coveragestores

# create coveragestore
curl -v -u admin:geoserver -XPOST -H "Content-Type: application/json" --data "@CoverageStores.json" http://localhost:9080/geoserver/rest/workspaces/pairs/coveragestores

# delete a store
# curl -v -u admin:geoserver -XDELETE -H "Content-Type: application/json"  http://localhost:9080/geoserver/rest/workspaces/pairs/coveragestores/pairsplugin

# query coverages for a store. The json response can be used to create the coverage, see Coverage.json
curl -v -u admin:geoserver -H "Content-Type: application/json" http://localhost:8080/geoserver/rest/workspaces/pairs/coveragestores/pairsplugin/coverages/pairspluginlayer
curl -v -u admin:geoserver -H "Content-Type: application/json" http://localhost:8080/geoserver/rest/workspaces/pairs/coveragestores/pairscoverage1/coverages/pairsplugincoverage1

# Create Coverage
curl -v -u admin:geoserver -H "Content-Type: application/json" http://localhost:9080/geoserver/rest/workspaces/pairs/coveragestores/pairsplugin/coverages.json

# query and delete
curl -v -u admin:geoserver -XPOST -H "Content-Type: application/json" --data "@Coverage.json" http://localhost:9080/geoserver/rest/workspaces/pairs/coveragestores/pairsplugin/coverages
# curl -v -u admin:geoserver -XDELETE -H "Content-Type: application/json"  http://localhost:9080/geoserver/rest/workspaces/pairs/coveragestores/pairsplugin/coverages/pairspluginlayer
