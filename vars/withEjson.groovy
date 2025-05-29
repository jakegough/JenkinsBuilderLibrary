def run(String ejsonCredentialsId, Closure callback) {

    withCredentials([usernamePassword(
        credentialsId: ejsonCredentialsId,
        usernameVariable: 'ejson_public_key',
        passwordVariable: 'ejson_private_key')]) {

        def ejsonEnvVar = "EJK_${ejson_public_key}=${ejson_private_key}"
        withEnv([ejsonEnvVar]) {
            callback()
        }
    }
}
