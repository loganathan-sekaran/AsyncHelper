#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    mvn deploy jacoco:report coveralls:report -X --settings $GPG_DIR/settings.xml -DperformRelease=true
    exit $?
fi