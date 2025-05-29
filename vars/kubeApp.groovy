def build(Map args = [:]) {

    def gitHubUsername = args.get('gitHubUsername', 'jakegough-homelab');
    def gitHubRepository = args.get('gitHubRepository', 'missing_gitHubRepository');
    def gitHubTokenCredentialsId = args.get('gitHubTokenCredentialsId', 'github-jakegough-jaytwo-token');
    def dockerRegistryCredentialsId = args.get('dockerRegistryCredentialsId', 'homelab-docker-container-registry');
    def dockerRegistry = args.get('dockerRegistry', 'container-registry.homelab.jaytwo.com');
    def dockerImageName = args.get('dockerImageName', 'missing_dockerImageName');
    def devCluster = args.get('devCluster', 'k3s-general');
    def devNamespace = args.get('devNamespace', '');
    def prodCluster = args.get('prodCluster', 'k3s-general');
    def prodNamespace = args.get('prodNamespace', '');
    def ejsonCredentialsId = args.get('ejsonCredentialsId', 'missing_ejsonCredentialsId');
    def ejsonPublicKey = args.get('ejsonPublicKey', 'missing_ejsonPublicKey');

    helper.run('linux && make && docker', {
        def timestamp = helper.getTimestamp()
        def safeJobName = helper.getSafeJobName()
        def dockerLocalTag = "jenkins__${safeJobName}__${timestamp}"
        def dockerBuilderTag = dockerLocalTag + "__builder"

        withEnv(["DOCKER_TAG=${dockerLocalTag}", "TIMESTAMP=${timestamp}"]) {
            try {
                stage ('Build') {
                    sh "make docker-builder"
                }

                withCredentials([string(credentialsId: ejsonCredentialsId, variable: "EJK_" + ejsonPublicKey)]) {
                    docker.image(dockerBuilderTag).inside() {
                        stage ('Unit Tests') {
                            sh "make unit-test"
                        }

                        stage ('Integration Tests') { }
                    }
                }

                if (branches.isDeploymentBranch()) {
                    stage ('Push Image') {
                        dockerHelper.login(dockerRegistryCredentialsId, dockerRegistry)

                        def devTag = "$dockerImageName:latest-dev";
                        dockerHelper.tag(dockerLocalTag, devTag)
                        dockerHelper.pushImage(devTag)
                        dockerHelper.removeImage(devTag)
                    }

                    if (devNamespace && branches.isDevelopBranch()) {
                        stage ('Deploy Dev') {
                            kubectl.inside(devCluster) {
                                sh """
                                    kubectl rollout restart deployment/app -n $devNamespace
                                    kubectl rollout status deployment/app -n $devNamespace --timeout=5m
                                """}
                        }
                    }

                    stage ('Integration Tests') { }

                    if (prodNamespace && branches.isMasterBranch()) {
                        stage ('Deploy Prod') {
                            def prodTag = "$dockerImageName:latest-prod";
                            dockerHelper.tag(dockerLocalTag, prodTag)
                            dockerHelper.pushImage(prodTag)
                            dockerHelper.removeImage(prodTag)

                            kubectl.inside(prodCluster) {
                                sh """
                                    kubectl rollout restart deployment/app -n $prodNamespace
                                    kubectl rollout status deployment/app -n $prodNamespace --timeout=5m
                                """}
                        }
                        stage ('Integration Tests') { }
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
