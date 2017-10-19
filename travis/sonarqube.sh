#!/usr/bin/env bash

function main {
    require_no_pull_request
    ./gradlew sonarqube
}

function require_no_pull_request {
    if [ -v TRAVIS_PULL_REQUEST -a "$TRAVIS_PULL_REQUEST" != "false" ]; then
        echo "Building pull request. Skipping sonarqube analysis"
        exit 0
    fi
}

main
