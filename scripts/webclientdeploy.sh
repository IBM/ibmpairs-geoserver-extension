#!/bin/bash
FOLDER=/pairs_data/datapreview
VERSION=0_5
CLIENT_NAME="pairsclient"

if [ $# -ge 1 ]; then
    VERSION=$1
elif [ $# -ge 2 ]; then
    PORT=$2
fi

# scp webclient/package.json hduser@pairs-alpha:$FOLDER/view/

scp webclient/pairsclient$VERSION.html hduser@pairs-alpha:${FOLDER}/${CLIENT_NAME}.html
scp webclient/pairsclient$VERSION.js hduser@pairs-alpha:${FOLDER}/${CLIENT_NAME}${VERSION}.js

# Can't get the env correct, tried adding .bash_profile and .profile on pairs_alpha to source .bashrc
# ssh hduser@pairs-alpha bash << EOF 
#  env
#  cd fjm/view;
#  pwd
# EOF