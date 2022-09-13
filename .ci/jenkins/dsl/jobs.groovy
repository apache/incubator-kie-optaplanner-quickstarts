/*
* This file is describing all the Jenkins jobs in the DSL format (see https://plugins.jenkins.io/job-dsl/)
* needed by the Kogito pipelines.
*
* The main part of Jenkins job generation is defined into the https://github.com/kiegroup/kogito-pipelines repository.
*
* This file is making use of shared libraries defined in
* https://github.com/kiegroup/kogito-pipelines/tree/main/dsl/seed/src/main/groovy/org/kie/jenkins/jobdsl.
*/

import org.kie.jenkins.jobdsl.model.Folder
import org.kie.jenkins.jobdsl.KogitoJobTemplate
import org.kie.jenkins.jobdsl.KogitoJobUtils

jenkins_path = '.ci/jenkins'

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
                    BUILD_MVN_OPTS_CURRENT: '-Dfull',
                    OPTAPLANNER_BUILD_MVN_OPTS_UPSTREAM: '-Dfull',
                ]
            ]
        ],
    ]
}

// Optaplanner PR checks
KogitoJobUtils.createAllEnvsPerRepoPRJobs(this) { jobFolder -> getMultijobPRConfig() }

// Init branch
setupInitBranchJob()

// Tools
KogitoJobUtils.createQuarkusUpdateToolsJob(this, 'optaplanner-quickstarts', [
    properties: [ 'version.io.quarkus' ],
], [
    // Escaping quotes so it is correctly handled by Json marshalling/unmarshalling
    regex: [ 'id \\"io.quarkus\\" version', 'def quarkusVersion =' ]
])

void setupInitBranchJob() {
    def jobParams = KogitoJobUtils.getBasicJobParams(this, 'optaplanner-quickstarts', Folder.INIT_BRANCH, "${jenkins_path}/Jenkinsfile.init-branch", 'OptaPlanner Quickstarts Init Branch')
    KogitoJobUtils.setupJobParamsDefaultMavenConfiguration(this, jobParams)
    jobParams.env.putAll([
        REPO_NAME: 'optaplanner-quickstarts',
        JENKINS_EMAIL_CREDS_ID: "${JENKINS_EMAIL_CREDS_ID}",

        GIT_AUTHOR: "${GIT_AUTHOR_NAME}",
        AUTHOR_CREDS_ID: "${GIT_AUTHOR_CREDENTIALS_ID}",

        MAVEN_SETTINGS_CONFIG_FILE_ID: "${MAVEN_SETTINGS_FILE_ID}",
        MAVEN_DEPENDENCIES_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",
        MAVEN_DEPLOY_REPOSITORY: "${MAVEN_ARTIFACTS_REPOSITORY}",
    ])
    KogitoJobTemplate.createPipelineJob(this, jobParams)?.with {
        parameters {
            stringParam('DISPLAY_NAME', '', 'Setup a specific build display name')

            stringParam('BUILD_BRANCH_NAME', "${GIT_BRANCH}", 'Set the Git branch to checkout')

            stringParam('OPTAPLANNER_VERSION', '', 'OptaPlanner version to set.')

            booleanParam('SEND_NOTIFICATION', false, 'In case you want the pipeline to send a notification on CI channel for this run.')
        }
    }
}