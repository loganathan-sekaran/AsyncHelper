#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    mvn deploy --settings $GPG_DIR/settings.xml -DperformRelease=true jacoco:report coveralls:report
    exit $?
fi