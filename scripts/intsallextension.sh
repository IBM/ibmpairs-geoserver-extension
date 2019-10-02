#!/bin/bash

HOST="http://test=pairs=dev01.watson.ibm.com:8082"
WS=pairs
DATASTORE_PATH="workspaces/$WS/datastores"

HELP="install hosturl [-u datastore url] [-n store name]"
PARAMS=""
while (( "$#" )); do
  case "$1" in
    -h|--help)
        echo $HELP
      exit 1
      ;;
    -H|--host)
      HOST=$2
      shift 2
      ;;
    --) # end argument parsing
      shift
      break
      ;;
    -*|--*=) # unsupported flags
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

BODY_FILE="scripts/pairsdatastore.json"
ACCEPTS="accepts: application/json"

CMD="curl $HOST/$PATH $ACCEPTS
		$EXECUTABLE load \
		$BIN_DATA \
		-createOverview=$CREATE_OVERVIEW
		-server=$HBASE_SERVER \
		-tableName=$TABLE \
		-dtype=$TYPE \
		-date=$DATE \
		-family=$FN \
		-qualifier=$FQ \
		-layer=$LAYER "

echo CMD = $CMD

RESULT=$($CMD)
echo RESULT