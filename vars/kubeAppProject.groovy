def build(Map args = [:]) {

    def dockerRegistryCredentialsId = args.get('dockerRegistryCredentialsId', 'homelab-docker-container-registry');
    def dockerRegistry = args.get('dockerRegistry', 'container-registry.homelab.jaytwo.com');
    def dockerImageName = args.get('dockerImageName', 'missing_dockerImageName');
    def devCluster = args.get('devCluster', 'k3s-general');
    def devNamespace = args.get('devNamespace', '');
    def prodCluster = args.get('prodCluster', 'k3s-general');
    def prodNamespace = args.get('prodNamespace', '');
    def ejsonPublicKey = args.get('ejsonPublicKey', 'missing_ejsonPublicKey');
    def devEnvironment = args.get('devEnvironment', 'Development');
    def devDockerImageTag = args.get('devDockerImageTag', 'latest-dev');
    def prodEnvironment = args.get('prodEnvironment', 'Production');
    def prodDockerImageTag = args.get('prodDockerImageTag', 'latest-prod');

    helper.gitHubUsername = args.get('gitHubUsername', 'jakegough-homelab');
    helper.gitHubRepository = args.get('gitHubRepository', 'missing_gitHubRepository');
    helper.gitHubTokenCredentialsId = args.get('gitHubTokenCredentialsId', 'github-jakegough-homelab-token');

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

                withEjson(ejsonPublicKey) {
                    docker.image(dockerBuilderTag).inside() {
                        stage ('Unit Tests') {
                            sh "make unit-test"
                        }

                        stage ('Integration Tests') {
                            makeIntegrationTest(ejsonPublicKey, "")
                        }
                    }
                }

                if (branches.isDeploymentBranch()) {
                    stage ('Push Image') {
                        dockerHelper.login(dockerRegistryCredentialsId, dockerRegistry)
                        dockerTagAndPush(dockerLocalTag, "$dockerRegistry/$dockerImageName:$devDockerImageTag")
                    }

                    if (branches.isDevelopBranch() && devNamespace) {
                        stage ('Deploy Dev') {
                            kubectlRolloutRestart(devCluster, devNamespace);
                        }

                        stage ('Integration Tests') {
                            docker.image(dockerBuilderTag).inside() {
                                makeIntegrationTest(ejsonPublicKey, devEnvironment)
                            }
                        }
                    }

                    if (branches.isMasterBranch() && prodNamespace) {
                        stage ('Promote Image') {
                            dockerHelper.login(dockerRegistryCredentialsId, dockerRegistry)
                            dockerTagAndPush(dockerLocalTag, "$dockerRegistry/$dockerImageName:$prodDockerImageTag")
                        }

                        stage ('Deploy Prod') {
                            kubectlRolloutRestart(prodCluster, prodNamespace);
                        }

                        stage ('Integration Tests') {
                            docker.image(dockerBuilderTag).inside() {
                                makeIntegrationTest(ejsonPublicKey, prodEnvironment)
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

def makeIntegrationTest(String ejsonPublicKey, String prodEnvironment) {
    withEjson(ejsonPublicKey) {
        sh "make integration-test TEST_ENV=$prodEnvironment"
    }
}
