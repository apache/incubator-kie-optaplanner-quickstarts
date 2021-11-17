import org.kie.jenkins.jobdsl.templates.KogitoJobTemplate
import org.kie.jenkins.jobdsl.KogitoJobUtils

BUILDCHAIN_CONFIG_BRANCH = '%{process.env.GITHUB_BASE_REF.replace(/(\\d*)\\.(.*)\\.(.*)/g, (m, n1, n2, n3) => `\\${+n1-7}.\\${n2}.\\${n3}`).replace("development", "main")}'

def getDefaultJobParams(String repoName = 'optaplanner-quickstarts') {
    return KogitoJobTemplate.getDefaultJobParams(this, repoName)
}

Map getMultijobPRConfig() {
    return [
        parallel: true,
        buildchain: true,
        jobs : [
            [
                id: 'optaplanner-quickstarts',
                primary: true,
                env : [
                    // Sonarcloud analysis only on main branch
                    // As we have only Community edition
                    // Currently disabled
                    DISABLE_SONARCLOUD: true,
                    // DISABLE_SONARCLOUD: !Utils.isMainBranch(this),
                    OPTAPLANNER_BUILD_MVN_OPTS_UPSTREAM: '-Dfull',
                    BUILDCHAIN_CONFIG_BRANCH: BUILDCHAIN_CONFIG_BRANCH
                ]
            ]
        ],
    ]
}

// Optaplanner PR checks
setupMultijobPrDefaultChecks()
setupMultijobPrNativeChecks()
setupMultijobPrLTSChecks()

// Tools
KogitoJobUtils.createQuarkusUpdateToolsJob(this, 'optaplanner-quickstarts', 'OptaPlanner Quickstarts', [
    properties: [ 'version.io.quarkus' ],
], [
    // Escaping quotes so it is correctly handled by Json marshalling/unmarshalling
    regex: [ 'id \\"io.quarkus\\" version', 'def quarkusVersion =' ]
])

/////////////////////////////////////////////////////////////////
// Methods
/////////////////////////////////////////////////////////////////

void setupMultijobPrDefaultChecks() {
    KogitoJobTemplate.createMultijobPRJobs(this, getMultijobPRConfig()) { return getDefaultJobParams() }
}

void setupMultijobPrNativeChecks() {
    KogitoJobTemplate.createMultijobNativePRJobs(this, getMultijobPRConfig()) { return getDefaultJobParams() }
}

void setupMultijobPrLTSChecks() {
    KogitoJobTemplate.createMultijobLTSPRJobs(this, getMultijobPRConfig()) { return getDefaultJobParams() }
}
