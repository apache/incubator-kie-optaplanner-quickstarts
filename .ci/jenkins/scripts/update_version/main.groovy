void execute(def pipelinesCommon) {
    maven.mvnSetVersionProperty(pipelinesCommon.getDefaultMavenCommand(), 'version.org.optaplanner', pipelinesCommon.getOptaPlannerVersion())
    maven.mvnVersionsUpdateParentAndChildModules(pipelinesCommon.getDefaultMavenCommand(), pipelinesCommon.getOptaPlannerVersion(), !pipelinesCommon.isRelease())
    
    gradleVersionsUpdate(pipelinesCommon.getOptaPlannerVersion())
}

void gradleVersionsUpdate(String newVersion) {
    sh "find . -name build.gradle -exec sed -i -E 's/def optaplannerVersion = \"[^\"\\s]+\"/def optaplannerVersion = \"${newVersion}\"/' {} \\;"
}

return this
