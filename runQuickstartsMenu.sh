#!/bin/bash

# Change directory to the directory of the script
cd $(dirname $) || exit

mvn clean install -N -DskipTests
cd build/all-quickstarts || exit
mvn quarkus:dev
