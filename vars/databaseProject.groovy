def build(Map args = [:]) {

    def gitHubUsername = args.get('gitHubUsername', 'jakegough-homelab');
    def gitHubRepository = args.get('gitHubRepository', 'missing_gitHubRepository');
    def gitHubTokenCredentialsId = args.get('gitHubTokenCredentialsId', 'github-jakegough-jaytwo-token');
    def ejsonCredentialsId = args.get('ejsonCredentialsId', 'missing_ejsonCredentialsId');

    helper.run('linux && make && docker', {
        try {
            stage ('Build') {
                sh "make clean build"
            }
            stage ('Test Migration') {
                sh "make test"
            }
            if (branches.isDeploymentBranch()){
                withEjson(ejsonCredentialsId) {
                    if (branches.isDevelopBranch()){
                        stage ('Migrate Dev') {
                            sh "make migrate-dev"
                        }
                    }

                    if (branches.isMasterBranch()){
                        stage ('Migrate Prod') {
                            sh "make migrate-prod"
                        }
                    }
                }
            }
        }
        finally {
            sh "make clean"
        }
    })
}
