void execute(def pipelinesCommon) {
    if (pipelinesCommon.isRelease()) {
        assert !sh (script:
                'grep -Rn "SNAPSHOT" --include={pom.xml,build.gradle} | ' +
                'grep -v -e "0.1.0-SNAPSHOT" -e ">1.0-SNAPSHOT" | ' +
                'cat', returnStdout: true)
    }
    if (pipelinesCommon.isCreatePr()) {
        assert !sh (script:
                'grep -Rn "SNAPSHOT" --include={pom.xml,build.gradle} | ' +
                'grep -v -e "${getProjectVersion()}" | ' +
                'cat', returnStdout: true)
    }
}

return this
