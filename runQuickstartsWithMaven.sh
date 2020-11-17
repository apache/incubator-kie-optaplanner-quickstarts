#!/bin/bash

# Change directory to the directory of the script
cd $(dirname $) || exit

mvn verify -DskipTests
cd build/all-quickstarts || exit
mvn quarkus:dev -Dstartup-open-browser=true
