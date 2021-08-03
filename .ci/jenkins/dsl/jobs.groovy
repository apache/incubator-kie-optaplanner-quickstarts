import org.kie.jenkins.jobdsl.templates.KogitoJobTemplate
import org.kie.jenkins.jobdsl.KogitoConstants
import org.kie.jenkins.jobdsl.Utils
import org.kie.jenkins.jobdsl.KogitoJobType

JENKINS_PATH = '.ci/jenkins'
BUILD_CHAIN_JENKINS_PATH = "${JENKINS_PATH}/Jenkinsfile.buildchain"
PR_REPO_URL = 'https://github.com/kiegroup/optaplanner'

def getDefaultJobParams(String repoName = 'optaplanner-quickstarts') {
    return KogitoJobTemplate.getDefaultJobParams(this, repoName)
}

Map getMultijobPRConfig() {
    return [
        parallel: true,
        jobs : [
            [
                id: 'optaplanner',
                primary: true,
                // TODO remove once https://issues.redhat.com/browse/KOGITO-4113 is done 
                // as it will become the default path
                jenkinsfile: BUILD_CHAIN_JENKINS_PATH,
                git: [
                    repo_url: PR_REPO_URL
                ],
            ], [
                id: 'apps',
                repository: 'kogito-apps',
                jenkinsfile: BUILD_CHAIN_JENKINS_PATH,
                git: [
                    repo_url: PR_REPO_URL
                ],
            ], [
                id: 'examples',
                repository: 'kogito-examples',
                jenkinsfile: BUILD_CHAIN_JENKINS_PATH,
                git: [
                    repo_url: PR_REPO_URL
                ],
            ], [
                id: 'runtimes',
                repository: 'kogito-runtimes',
                jenkinsfile: BUILD_CHAIN_JENKINS_PATH,
                git: [
                    repo_url: PR_REPO_URL
                ],
            ], [
                id: 'rostering',
                repository: 'optaweb-employee-rostering',
                jenkinsfile: BUILD_CHAIN_JENKINS_PATH,
                git: [
                    repo_url: PR_REPO_URL
                ],
            ], [
                id: 'routing',
                repository: 'optaweb-vehicle-routing',
                jenkinsfile: BUILD_CHAIN_JENKINS_PATH,
                git: [
                    repo_url: PR_REPO_URL
                ],
            ], [
                id: 'quickstarts',
                repository: 'optaplanner-quickstarts',
                jenkinsfile: BUILD_CHAIN_JENKINS_PATH,
                git: [
                    repo_url: PR_REPO_URL
                ],
            ]
        ],
        extraEnv : [
            // Sonarcloud analysis only on main branch
            // As we have only Community edition
            ENABLE_SONARCLOUD: Utils.isMainBranch(this)
        ]
    ]
}

def getJobParams(String jobName, String jobFolder, String jenkinsfileName, String jobDescription = '') {
    def jobParams = getDefaultJobParams()
    jobParams.job.name = jobName
    jobParams.job.folder = jobFolder
    jobParams.jenkinsfile = jenkinsfileName
    if (jobDescription) {
        jobParams.job.description = jobDescription
    }
    return jobParams
}


// Optaplanner PR checks
setupMultijobPrDefaultChecks()
setupMultijobPrNativeChecks()
setupMultijobPrLTSChecks()


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
