@ECHO OFF
setLocal enableExtensions enableDelayedExpansion

mvnw verify -DskipTests && ^
mvnw -f build\quickstarts-showcase quarkus:dev -Dstartup-open-browser=true
