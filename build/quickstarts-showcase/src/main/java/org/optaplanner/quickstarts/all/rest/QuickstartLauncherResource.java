/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.quickstarts.all.rest;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.optaplanner.quickstarts.all.domain.QuickstartMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@Path("quickstart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuickstartLauncherResource {

    protected static final Logger logger = LoggerFactory.getLogger(QuickstartLauncherResource.class);

    @ConfigProperty(name = "startup-open-browser", defaultValue = "false")
    boolean startupOpenBrowser;
    @ConfigProperty(name = "quarkus.http.port")
    int httpPort;

    private List<QuickstartMeta> quickstartMetaList;
    private boolean development;
    private int nextPort = 8081;
    private File baseDirectory;

    private Map<Integer, Process> portToProcessMap;

    public void setup(@Observes StartupEvent startupEvent) {
        quickstartMetaList = Arrays.asList(
                new QuickstartMeta("quarkus-school-timetabling"),
                new QuickstartMeta("quarkus-facility-location"),
                new QuickstartMeta("quarkus-maintenance-scheduling"),
                new QuickstartMeta("quarkus-vaccination-scheduling"),
                new QuickstartMeta("quarkus-factorio-layout"));
        File workingDirectory;
        try {
            workingDirectory = new File(".").getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException("Could not determine the workingDirectory.", e);
        }
        if (workingDirectory.getName().equals("target")) {
            baseDirectory = new File(workingDirectory, "../../..");
            development = true;
        } else {
            baseDirectory = new File(workingDirectory, "binaries");
            development = false;
        }
        portToProcessMap = new HashMap<>(quickstartMetaList.size());
        try {
            baseDirectory = baseDirectory.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException("Could not canonicalize baseDirectory (" + baseDirectory + ").", e);
        }
        if (startupOpenBrowser) {
            openInBrowser(httpPort);
        }
    }

    public void shutdown(@Observes ShutdownEvent shutdownEvent) {
        for (QuickstartMeta quickstartMeta : quickstartMetaList) {
            for (int port : quickstartMeta.getPorts()) {
                portToProcessMap.remove(port).destroy();
            }
            quickstartMeta.getPorts().clear();
        }
    }

    @GET
    public List<QuickstartMeta> getQuickstartMetaList() {
        return quickstartMetaList;
    }

    @Path("{quickstartId}/launch")
    @POST
    public void launchQuickstart(@PathParam("quickstartId") String quickstartId) {
        QuickstartMeta quickstartMeta = quickstartMetaList.stream()
                .filter(quickstartMeta_ -> quickstartMeta_.getId().equals(quickstartId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The quickstartId (" + quickstartId + ") doesn't exist."));
        logger.info("Starting quickstart ({})...", quickstartId);

        ProcessBuilder processBuilder;
        int port = this.nextPort;
        String portArg = "-Dquarkus.http.port=" + port;
        // CORS allows the JS to detect when the server has started
        String corsArg = "-Dquarkus.http.cors=true";
        this.nextPort++;
        if (development) {
            String mvnCommand = findMvnCommand(baseDirectory);
            processBuilder = new ProcessBuilder(mvnCommand, "quarkus:dev", portArg, corsArg, "-Ddebug=false");
        } else {
            processBuilder = new ProcessBuilder("java", portArg, corsArg, "-jar",
                    getQuickstartRunnerJar(quickstartId).getAbsolutePath());
        }
        processBuilder.directory(new File(baseDirectory, quickstartId));
        processBuilder.inheritIO();
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed starting the subprocess for quickstart (" + quickstartId + ").\n"
                    + (development ? "Maybe define environment variable M3_HOME and check if \"mvn --version\" works."
                            : "Maybe install Java and check if \"java --version\" works."),
                    e);
        }
        portToProcessMap.put(port, process);
        quickstartMeta.getPorts().add(port);
    }

    private File getQuickstartRunnerJar(String quickstartId) {
        File quickstartRunnerJar = FileSystems.getDefault().getPath(baseDirectory.getAbsolutePath(), quickstartId,
                "quarkus-app", "quarkus-run.jar").toFile();
        if (!quickstartRunnerJar.exists()) {
            throw new IllegalStateException(
                    "The quickstart (" + quickstartId + ") runner JAR file does not exist ("
                            + quickstartRunnerJar.getAbsolutePath() + ").");
        }
        return quickstartRunnerJar;
    }

    private String findMvnCommand(File baseDirectory) {
        Optional<String> maybeMavenHome = Stream.of("M3_HOME", "M2_HOME", "MAVEN_HOME")
                .map(System::getenv)
                .filter(s -> s != null)
                .findFirst();
        if (!maybeMavenHome.isPresent()) {
            logger.warn("Cannot find Maven home. Falling back to Maven Wrapper."
                    + " Maybe define environment variable M3_HOME instead.");
            String scriptFileName = System.getProperty("os.name").startsWith("Windows") ? "mvnw.cmd" : "mvnw";
            return new File(baseDirectory, scriptFileName).getAbsolutePath();
        }
        String mavenHome = maybeMavenHome.get().replaceFirst("^~", System.getProperty("user.home"));
        File mvnFile = new File(mavenHome, "bin/mvn");
        try {
            mvnFile = mvnFile.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException("Could not canonicalize mvnFile (" + mvnFile + ").\n"
                    + "Maybe check your environment variable M3_HOME/M2_HOME/MAVEN_HOME (" + mavenHome + ").", e);
        }
        return mvnFile.getAbsolutePath();
    }

    @Path("{quickstartId}/stop/{port}")
    @DELETE
    public void stopQuickstart(@PathParam("quickstartId") String quickstartId, @PathParam("port") int port) {
        QuickstartMeta quickstartMeta = quickstartMetaList.stream()
                .filter(quickstartMeta_ -> quickstartMeta_.getPorts().contains((Object) port))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The process on port (" + port
                        + ") was already destroyed or never existed."));
        if (!quickstartMeta.getId().equals(quickstartId)) {
            throw new IllegalArgumentException("The quickstartId (" + quickstartId
                    + ") does not match the quickstart (" + quickstartMeta.getId() + ") on port (" + port + ").");
        }
        quickstartMeta.getPorts().remove((Object) port);
        Process process = portToProcessMap.remove(port);
        process.destroy();
    }

    private void openInBrowser(int port) {
        String url = "http://localhost:" + port;
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
            logger.warn("There is no default browser to show the URL (" + url + ").");
            return;
        }
        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed opening the default browser to show the URL (" + url + ").", e);
        }
    }

}
