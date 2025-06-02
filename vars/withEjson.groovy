def call(String ejsonPublicKey, Closure callback) {
    if (!ejsonPublicKey?.trim()) {
        error("ejsonPublicKey must not be null or empty")
    }

    echo "Loading EJSON private key into EJK_${ejsonPublicKey}"

    withCredentials([string(credentialsId: "EJK_$ejsonPublicKey", variable: 'ejson_private_key')]) {
        withEnv(["EJK_${ejsonPublicKey}=${ejson_private_key}"]) {
            callback()
        }
    }
}
