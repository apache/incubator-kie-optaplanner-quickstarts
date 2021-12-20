void execute(def pipelinesCommon) {
    githubscm.findAndStageNotIgnoredFiles('pom.xml')
    githubscm.findAndStageNotIgnoredFiles('build.gradle')
}

return this
