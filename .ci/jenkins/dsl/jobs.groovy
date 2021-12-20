import org.kie.jenkins.jobdsl.model.Folder
import org.kie.jenkins.jobdsl.templates.KogitoJobTemplate
import org.kie.jenkins.jobdsl.KogitoJobUtils

OPTAPLANNER_QUICKSTARTS = 'optaplanner-quickstarts'

def getDefaultJobParams(String repoName = OPTAPLANNER_QUICKSTARTS) {
    return KogitoJobUtils.getDefaultJobParams(this, repoName)
}

Map getMultijobPRConfig() {
    return [
        parallel: true,
        buildchain: true,
        jobs : [
            [
                id: OPTAPLANNER_QUICKSTARTS,
                primary: true,
                env : [
                    // Sonarcloud analysis only on main branch
                    // As we have only Community edition
                    // Currently disabled
                    DISABLE_SONARCLOUD: true,
                    // DISABLE_SONARCLOUD: !Utils.isMainBranch(this),
                    BUILD_MVN_OPTS_CURRENT: '-Dfull',
                    OPTAPLANNER_BUILD_MVN_OPTS_UPSTREAM: '-Dfull',
                ]
            ]
        ],
    ]
}

// PR checks
KogitoJobUtils.createAllEnvsPerRepoPRJobs(this, { jobFolder -> getMultijobPRConfig() }, { return getDefaultJobParams() })

// Create all Nightly jobs
KogitoJobUtils.createAllJobsForArtifactsRepository(this, 'kogito-runtimes', ['optaplanner'])

// Tools
KogitoJobUtils.createQuarkusUpdateToolsJob(this, OPTAPLANNER_QUICKSTARTS, [
    properties: [ 'version.io.quarkus' ],
], [
    // Escaping quotes so it is correctly handled by Json marshalling/unmarshalling
    regex: [ 'id \\"io.quarkus\\" version', 'def quarkusVersion =' ]
])
