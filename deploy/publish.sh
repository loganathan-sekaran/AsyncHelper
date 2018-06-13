#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
	cd Project/asynchelper
    mvn deploy --settings $GPG_DIR/settings.xml -DperformRelease=true -DskipTests=true
    exit $?
fi