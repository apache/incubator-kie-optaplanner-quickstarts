/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.acme.schooltimetabling;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

class JarWithDependenciesIT {

    // The property is set by maven-failsafe-plugin.
    private static final Path PATH_TO_JAR_WITH_DEPENDENCIES =
            Paths.get("target", System.getProperty("artifactName") + ".jar");

    private static final Path PATH_TO_JAVA_EXECUTABLE_UNIX =
            Paths.get(System.getenv("JAVA_HOME"), "bin", "java");
    private static final Path PATH_TO_JAVA_EXECUTABLE_WINDOWS =
            Paths.get(System.getenv("JAVA_HOME"), "bin", "java.exe");

    private Path pathToJavaExecutable;

    @BeforeEach
    void setJavaPath() {
        if (PATH_TO_JAVA_EXECUTABLE_WINDOWS.toFile().exists()) {
            pathToJavaExecutable = PATH_TO_JAVA_EXECUTABLE_WINDOWS;
        } else if (PATH_TO_JAVA_EXECUTABLE_UNIX.toFile().exists()) {
            pathToJavaExecutable = PATH_TO_JAVA_EXECUTABLE_UNIX;
        } else {
            Assertions.fail("Neither a Windows Java binary (" + PATH_TO_JAVA_EXECUTABLE_WINDOWS + ")"
                    + " nor a Unix Java binary (" + PATH_TO_JAVA_EXECUTABLE_UNIX + ") was found."
                    + System.lineSeparator() + "Maybe set JAVA_HOME?");
        }
    }

    @Test
    void runJarWithDependencies() throws IOException {
        Assumptions.assumeThat(PATH_TO_JAR_WITH_DEPENDENCIES)
                .as("Executable JAR not found. Maybe ensure maven-assembly-plugin was run beforehand?")
                .exists();

        Process process = new ProcessBuilder()
                .command(pathToJavaExecutable.toString(),
                        "-jar",
                        PATH_TO_JAR_WITH_DEPENDENCIES.toString())
                .inheritIO()
                .start();
        try {
            process.waitFor(1, TimeUnit.MINUTES);
            if (process.isAlive()) {
                Assertions.fail("Executable JAR timed out.");
            }
            Assertions.assertThat(process.exitValue())
                    .as("Executable JAR exited abnormally.")
                    .isEqualTo(0);
        } catch (InterruptedException e) {
            Assertions.fail("Waiting for executable JAR to finish was interrupted.", e);
        } finally {
            process.destroyForcibly();
        }
    }

}
