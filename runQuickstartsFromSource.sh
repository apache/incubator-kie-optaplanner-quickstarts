#!/bin/bash

# Change directory to the directory of the script
cd "$(dirname $0)" || exit

./mvnw verify -DskipTests &&
  ./mvnw -f build/quickstarts-showcase quarkus:dev -Dstartup-open-browser=true
