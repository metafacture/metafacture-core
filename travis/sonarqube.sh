#!/usr/bin/env bash
#
# Copyright 2017, 2018 Christoph Böhme
#
# Licensed under the Apache License, Version 2.0 the "License";
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

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
