def build(Map args = [:]) {

    def gitHubUsername = args.get('gitHubUsername', 'jakegough-homelab');
    def gitHubRepository = args.get('gitHubRepository', 'missing_gitHubRepository');
    def gitHubTokenCredentialsId = args.get('gitHubTokenCredentialsId', 'github-jakegough-homelab-token');
    def dockerRegistryCredentialsId = args.get('dockerRegistryCredentialsId', 'homelab-docker-container-registry');
    def dockerRegistry = args.get('dockerRegistry', 'container-registry.homelab.jaytwo.com');
    def dockerImageName = args.get('dockerImageName', 'missing_dockerImageName');
    def devCluster = args.get('devCluster', 'k3s-general');
    def devNamespace = args.get('devNamespace', '');
    def prodCluster = args.get('prodCluster', 'k3s-general');
    def prodNamespace = args.get('prodNamespace', '');
    def ejsonCredentialsId = args.get('ejsonCredentialsId', 'missing_ejsonCredentialsId');

    helper.run('linux && make && docker', {
        def timestamp = helper.getTimestamp()
        def safeJobName = helper.getSafeJobName()
        def dockerLocalTag = "jenkins__${safeJobName}__${timestamp}"
        def dockerBuilderTag = dockerLocalTag + "__builder"

        withEnv(["DOCKER_TAG=${dockerLocalTag}", "DOCKER_BUILDER_TAG=${dockerBuilderTag}", "TIMESTAMP=${timestamp}"]) {
            try {
                stage ('Build') {
                    sh "make docker"
                }

                withEjson(ejsonCredentialsId) {
                    docker.image(dockerBuilderTag).inside() {
                        stage ('Unit Tests') {
                            sh "make unit-test"
                        }

                        stage ('Integration Tests') {
                            sh "make integration-test"
                        }
                    }
                }

                if (branches.isDeploymentBranch()) {
                    stage ('Push Image') {
                        dockerHelper.login(dockerRegistryCredentialsId, dockerRegistry)
                        dockerTagAndPush(dockerLocalTag, "$dockerRegistry/$dockerImageName:latest-dev")
                    }

                    if (branches.isDevelopBranch() && devNamespace) {
                        stage ('Deploy Dev') {
                            kubectlRolloutRestart(devCluster, devNamespace);
                        }

                        stage ('Integration Tests') {
                            withEjson(ejsonCredentialsId) {
                                docker.image(dockerBuilderTag).inside() {
                                    sh "make integration-test TEST_ENV=Production"
                                }
                            }
                        }
                    }

                    if (branches.isMasterBranch() && prodNamespace) {
                        stage ('Promote Image') {
                            dockerHelper.login(dockerRegistryCredentialsId, dockerRegistry)
                            dockerTagAndPush(dockerLocalTag, "$dockerRegistry/$dockerImageName:latest-prod")
                        }

                        stage ('Deploy Prod') {
                            kubectlRolloutRestart(prodCluster, prodNamespace);
                        }

                        stage ('Integration Tests') {
                            withEjson(ejsonCredentialsId) {
                                docker.image(dockerBuilderTag).inside() {
                                    sh "make integration-test TEST_ENV=Production"
                                }
                            }
                        }
                    }
                }
            }
            finally {
                // inside the withEnv()
                // sh "make docker-copy-from-builder-output"
                sh "make docker-clean"
            }
        }
    })
}

def dockerTagAndPush(String localTag, String remoteTag) {
    dockerHelper.tag(localTag, remoteTag)
    dockerHelper.pushImage(remoteTag)
    dockerHelper.removeImage(remoteTag)
}

def kubectlRolloutRestart(String clusterId, String namespace) {
    kubectl.inside(clusterId) {
        sh """
            kubectl rollout restart deployment/app -n $namespace
            kubectl rollout status deployment/app -n $namespace --timeout=5m
        """}
}
