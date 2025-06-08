def build(Map args = [:]) {

    def nuGetCredentialsId = args.get('nuGetCredentialsId', 'nuget-org-jaytwo');
    def enableTesterNet = args.get('enableTesterNet', false);

    helper.gitHubUsername = args.get('gitHubUsername', 'jakegough-jaytwo');
    helper.gitHubRepository = args.get('gitHubRepository', 'missing_gitHubRepository');
    helper.gitHubTokenCredentialsId = args.get('gitHubTokenCredentialsId', 'github-jakegough-jaytwo-token');
    helper.xunitTestResultsPattern = args.get('xunitTestResultsPattern', 'out/testResults/**/*.trx');
    helper.coberturaCoverageReport = args.get('coberturaCoverageReport', 'out/coverage/Cobertura.xml');
    helper.htmlCoverageReportDir = args.get('htmlCoverageReportDir', 'out/coverage/html');

    /*
    * requires make targets:
    * - docker-builder
    * - unit-test
    * - pack
    * - pack-beta
    * - nuget-check
    * - nuget-push
    * - docker-copy-from-builder-output
    * - docker-clean
    * - testernet-up (optional)
    * - testernet-clean (optional)
    *
    * also assumes the ejsonVariable is consistent with the secret configured in the docker-compose file
    */

    helper.run('linux && make && docker', {
        def timestamp = helper.getTimestamp()
        def safeJobName = helper.getSafeJobName()
        def dockerLocalTag = "jenkins__${safeJobName}__${timestamp}"
        def dockerBuilderTag = dockerLocalTag + "__builder"
        def dockerInsideArgs = ""

        echo "TIMESTAMP: $timestamp"
        echo "DOCKER_TAG: $dockerLocalTag"
        echo "DOCKER_BUILDER_TAG: $dockerBuilderTag"

        if (enableTesterNet) {
            def dockerComposeProjectName = dockerLocalTag + "__testernet"
            def dockerComposeNetwork = dockerComposeProjectName + "_default"
            dockerInsideArgs = "-e TEST_ENV=testernet --network ${dockerComposeNetwork}"
        }

        withEnv(["DOCKER_TAG=${dockerLocalTag}", "DOCKER_BUILDER_TAG=${dockerBuilderTag}", "TIMESTAMP=${timestamp}"]) {
            try {
                stage ('Build') {
                    sh "make docker-builder"

                    if (enableTesterNet) {
                        sh "make testernet-up"
                    }
                }
                docker.image(dockerBuilderTag).inside(dockerInsideArgs) {
                    stage ('Unit Test') {
                        sh "make unit-test"
                    }
                    stage ('Pack') {
                        if(branches.isMasterBranch()) {
                            sh "make pack"
                        } else {
                            sh "make pack-beta"
                        }
                    }
                    stage ('NuGet Check Version') {
                        sh "make nuget-check"
                    }
                    if (branches.isDeploymentBranch()) {
                        withCredentials([string(credentialsId: nuGetCredentialsId, variable: "NUGET_API_KEY")]) {
                            stage ('NuGet Push') {
                                sh "make nuget-push"
                            }
                        }
                    }
                }
            }
            finally {
                // inside the withEnv()
                sh "make docker-copy-from-builder-output"

                if (enableTesterNet) {
                    sh "make testernet-clean"
                }

                sh "make docker-clean"
            }
        }
    })
}
