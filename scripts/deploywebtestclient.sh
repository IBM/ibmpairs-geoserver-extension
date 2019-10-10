#!/bin/bash
FOLDER=/var/www/html/datapreview
VERSION=0_5
CLIENT_NAME="testclient"

if [ $# -ge 1 ]; then
    VERSION=$1
fi

# scp webclient/package.json hduser@pairs-alpha:$FOLDER/view/

scp webclient/testclient$VERSION.html test-pairs-dev01:${FOLDER}/${CLIENT_NAME}.html
scp webclient/testclient$VERSION.js test-pairs-dev01:${FOLDER}/${CLIENT_NAME}${VERSION}.js

# Can't get the env correct, tried adding .bash_profile and .profile on pairs_alpha to source .bashrc
# ssh hduser@pairs-alpha bash << EOF 
#  env
#  cd fjm/view;
#  pwd
# EOF