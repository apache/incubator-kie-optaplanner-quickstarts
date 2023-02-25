#!/bin/bash

JDK_JAVA_OPTIONS="-XX:+UseParallelGC -Xlog:class+load=info:loadedClasses.txt" gradle run
