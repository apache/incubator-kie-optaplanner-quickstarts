#!/bin/bash

# Change directory to the directory of the script
cd "$(dirname $0)" || exit

mvn verify -DskipTests
cd build/quickstarts-showcase || exit
mvn quarkus:dev -Dstartup-open-browser=true
