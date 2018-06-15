#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    mvn deploy jacoco:report coveralls:report --settings $GPG_DIR/settings.xml -DperformRelease=true -DskipTests=true -B
    exit $?
fi