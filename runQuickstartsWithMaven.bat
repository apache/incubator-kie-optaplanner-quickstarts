@ECHO OFF
setLocal enableExtensions enableDelayedExpansion

mvn verify -DskipTests
cd build\quickstarts-showcase
mvn quarkus:dev -Dstartup-open-browser=true
