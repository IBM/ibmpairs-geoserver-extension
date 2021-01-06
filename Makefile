#===============================================================
# IBM Confidential
#
# OCO Source Materials
#
# Copyright IBM Corp. 2018
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#===============================================================

.PHONY: package build compile clean test help

.DEFAULT_GOAL= help
SHELL:=bash
ORG:="pairs"
HOST=`hostname`
PROJECT:=$(shell basename `pwd`)
DESC:="pairs-geoserver-extension Jar build"

PROJECT_VERSION=$(shell cat pom.xml | grep "^.*<version>.*</version>$$"| head -1 | awk -F'[><]' '{print $$3}')
M2_DIR?=$(HOME)/.m2

DOCKER_DEV_IMAGE=maven:3.6.3-jdk-8-slim
DOCKER_VOLS=-v `pwd`:/usr/src/$(PROJECT) -v $(M2_DIR):/root/.m2 -w /usr/src/$(PROJECT)

DUMMYINCLUDE:=$(shell if [ ! -z "cicd/Makefile.release.mk" ]; then mkdir -p cicd && touch cicd/Makefile.release.mk; fi)
DUMMYINCLUDE:=$(shell if [ ! -z ".env" ]; then touch .env; fi)

# enable for auto release version
include .env
include cicd/Makefile.release.mk

help: # http://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
	@echo $(PROJECT):$(PROJECT_VERSION)
	@echo "==========================================================="
	@echo $(DESC)
	@echo
	@echo The targets available in this project are:
	@grep -h -E '^[a-zA-Z0-9_%/-\.]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\t\033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo

.env:
	@touch .env

env-%:
	@if [ "${${*}}" = "" ]; then \
		echo "Environment variable $* not set"; \
		exit 1; \
	fi

package: # The current package includes no test
	@mvn clean package -DskipTests=true

build: # A synonym for package (adds uniformity to Travis CI scripts). The current package includes no testmake
	@mvn clean package -DskipTests=true

compile:
	@mvn clean compile

clean: 
	@mvn clean

test:
	@mvn test

push: ## Pushes binaries to artifactory 
push: 
	@echo "push to artifactory:"
	@cicd/artifactoryUpload.sh -i "pairs-geoserver-extension-$(PROJECT_VERSION)-plugin.zip" -u ${ARTIFACTORY_USER} -k ${ARTIFACTORY_KEY} -r "wcp-pairsgeos-release-generic-local" -g "com.ibm.pairs" -a "pairs-geoserver-extension" -f "zip" -b "target"

checkout-deps: ## Checkout scripts required for cicd flow.
checkout-deps: env-GH_TOKEN
	@git clone --branch=master https://$(GH_TOKEN)@github.ibm.com/physical-analytics/pairs-cicd.git pairs-cicd
	@cp -pr pairs-cicd/* .
	@rm -rf pairs-cicd
