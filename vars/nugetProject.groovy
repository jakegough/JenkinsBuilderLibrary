def build(Map args = [:]) {

    def gitHubUsername = args.get('gitHubUsername', 'jakegough-jaytwo');
    def gitHubRepository = args.get('gitHubRepository', 'missing_gitHubRepository');
    def gitHubTokenCredentialsId = args.get('gitHubTokenCredentialsId', 'github-jakegough-jaytwo-token');
    def xunitTestResultsPattern = args.get('xunitTestResultsPattern', 'out/testResults/**/*.trx');
    def coberturaCoverageReport = args.get('coberturaCoverageReport', 'out/coverage/Cobertura.xml');
    def htmlCoverageReportDir = args.get('htmlCoverageReportDir', 'out/coverage/html');

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
    *
    * also assumes the ejsonVariable is consistent with the secret configured in the docker-compose file
    */

    def nuGetCredentialsId = 'nuget-org-jaytwo'

    helper.run('linux && make && docker', {
        def timestamp = helper.getTimestamp()
        def safeJobName = helper.getSafeJobName()
        def dockerLocalTag = "jenkins__${safeJobName}__${timestamp}"
        def dockerBuilderTag = dockerLocalTag + "__builder"

        withEnv(["DOCKER_TAG=${dockerLocalTag}", "DOCKER_BUILDER_TAG=${dockerBuilderTag}", "TIMESTAMP=${timestamp}"]) {
            try {
                stage ('Build') {
                    sh "make docker-builder"
                }
                docker.image(dockerBuilderTag).inside() {
                    stage ('Unit Test') {
                        sh "make unit-test"
                    }
                    stage ('Pack') {
                        if(branches.isMasterBranch()){
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
                sh "make docker-clean"
            }
        }
    })
}
