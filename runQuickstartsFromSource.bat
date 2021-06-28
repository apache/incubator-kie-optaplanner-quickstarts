@ECHO OFF
setLocal enableExtensions enableDelayedExpansion

cd build
mvnw verify -DskipTests && ^
mvnw -f quickstarts-showcase quarkus:dev -Dstartup-open-browser=true
cd ..
