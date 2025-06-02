def call(String ejsonPublicKey, Closure callback) {

    withCredentials([string(
        credentialsId: "EJK_$ejsonPublicKey",
        variable: 'ejson_private_key')]) {

        def ejsonEnvVar = "EJK_${ejsonPublicKey}=${ejson_private_key}"
        withEnv([ejsonEnvVar]) {
            callback()
        }
    }
}
