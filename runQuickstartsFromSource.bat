@ECHO OFF
setLocal enableExtensions enableDelayedExpansion

cd build
mvnw -f .. verify -DskipTests && cd build && ^
mvnw -f quickstarts-showcase quarkus:dev -Dstartup-open-browser=true
cd ..
cd ..
