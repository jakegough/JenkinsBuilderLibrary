def build(Map args = [:]) {

    def ejsonPublicKey = args.get('ejsonPublicKey', 'missing_ejsonPublicKey');

    helper.gitHubUsername = args.get('gitHubUsername', 'jakegough-homelab');
    helper.gitHubRepository = args.get('gitHubRepository', 'missing_gitHubRepository');
    helper.gitHubTokenCredentialsId = args.get('gitHubTokenCredentialsId', 'github-jakegough-homelab-token');

    helper.run('linux && make && docker', {
        try {
            withEjson(ejsonPublicKey) {
                stage ('Build') { sh "make clean build" }

                stage ('Test Migration') { sh "make test" }

                if (branches.isDevelopBranch()) {
                    stage ('Migrate Dev') { sh "make migrate-dev" }
                }

                if (branches.isMasterBranch()) {
                    stage ('Migrate Prod') { sh "make migrate-prod" }
                }
            }
        }
        finally {
            sh "make clean"
        }
    })
}
