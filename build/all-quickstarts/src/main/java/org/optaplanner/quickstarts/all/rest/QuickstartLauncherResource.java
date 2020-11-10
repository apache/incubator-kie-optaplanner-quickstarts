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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.event.Observes;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.optaplanner.quickstarts.all.domain.QuickstartMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("quickstart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuickstartLauncherResource {

    protected static final Logger logger = LoggerFactory.getLogger(QuickstartLauncherResource.class);

    private List<QuickstartMeta> quickstartMetaList;
    private boolean development;
    private int nextPort = 8081;
    private File baseDirectory;

    private Map<Integer, Process> runningPortToProcessMap;

    public void setup(@Observes StartupEvent startupEvent) {
        quickstartMetaList = new ArrayList<>();
        quickstartMetaList.add(new QuickstartMeta("quarkus-school-timetabling"));
        quickstartMetaList.add(new QuickstartMeta("quarkus-facility-location"));
        quickstartMetaList.add(new QuickstartMeta("quarkus-factorio-layout"));
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
            baseDirectory = new File(workingDirectory, "..");
            development = false;
        }
        runningPortToProcessMap = new HashMap<>(quickstartMetaList.size());
        try {
            baseDirectory = baseDirectory.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException("Could not canonicalize baseDirectory (" + baseDirectory + ").", e);
        }
//        openInBrowser(8080);
    }

    public void shutdown(@Observes ShutdownEvent shutdownEvent) {
        for (QuickstartMeta quickstartMeta : quickstartMetaList) {
            for (int runningPort : quickstartMeta.getRunningPorts()) {
                runningPortToProcessMap.remove(runningPort).destroy();
            }
            quickstartMeta.getRunningPorts().clear();
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
        this.nextPort++;
        if (development) {
            String mavenHome = System.getenv("M3_HOME");
            if (mavenHome == null) {
                mavenHome = System.getenv("M2_HOME");
                if (mavenHome == null) {
                    mavenHome = System.getenv("MAVEN_HOME");
                    if (mavenHome == null) {
                        throw new IllegalStateException("Cannot find Maven home.\n"
                                + "Maybe define environment variable M3_HOME to run from source.");
                    }
                }
            }
            mavenHome = mavenHome.replaceFirst("^~", System.getProperty("user.home"));
            File mvnFile = new File(mavenHome, "bin/mvn");
            try {
                mvnFile = mvnFile.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalStateException("Could not canonicalize mvnFile (" + mvnFile + ").\n"
                        + "Maybe check your environment variable M3_HOME/M2_HOME/MAVEN_HOME (" + mavenHome + ").", e);
            }
            processBuilder = new ProcessBuilder(mvnFile.getAbsolutePath(), "quarkus:dev", portArg, "-Ddebug=false");
        } else {
            processBuilder = new ProcessBuilder("java", "-jar",
                    "optaplanner-" + quickstartId + "-quickstart-1.0-SNAPSHOT-runner.jar",
                    portArg);
        }
        processBuilder.directory(new File(baseDirectory, quickstartId));
        processBuilder.inheritIO();
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed starting the subprocess for quickstart (" + quickstartId + ").", e);
        }
        runningPortToProcessMap.put(port, process);
        quickstartMeta.getRunningPorts().add(port);
//        openInBrowser(port);
    }

    @Path("{quickstartId}/stop/{runningPort}")
    @DELETE
    public void stopQuickstart(@PathParam("quickstartId") String quickstartId, @PathParam("runningPort") int runningPort) {
        QuickstartMeta quickstartMeta = quickstartMetaList.stream()
                .filter(quickstartMeta_ -> quickstartMeta_.getRunningPorts().contains((Object) runningPort))
                .findFirst()
                .orElse(null);
        if (quickstartMeta == null) {
            throw new IllegalArgumentException("The process on port (" + runningPort
                    + ") was already destroyed or never existed.");
        }
        quickstartMeta.getRunningPorts().remove((Object) runningPort);
        Process process = runningPortToProcessMap.remove(runningPort);
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
