#!/usr/bin/env bash

#
# Sonarqube analysis cannot be made when building pull requests because secure
# variables are not available in such builds. To prevent build  failures, the
# sonarqube target is only invoked if not building a pull request.
#

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
