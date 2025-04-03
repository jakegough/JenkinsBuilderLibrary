import groovy.transform.Field

@Field kubectlDockerImage="lachlanevenson/k8s-kubectl";

def withKubectl(kubeConfigFileCredentialsId, kubectlVersion, callback) {
    def kubectlVersionOrDefault = kubectlVersion ?: "latest";
    def kubectlImageTag = "$kubectlDockerImage:$kubectlVersionOrDefault"

    // requires plugin: https://plugins.jenkins.io/docker-plugin
    pluginHelper.verifyPluginExists('docker-plugin')

    docker.image(kubectlImageTag).pull()
    docker.image(kubectlImageTag).inside('--entrypoint ""') {
        withCredentials([file(credentialsId: kubeConfigFileCredentialsId, variable: 'KUBECONFIG')]) {
            callback()
        }
    }
}

def kubectlApply(kubeConfigFileCredentialsId, kubectlVersion, path, dryRun = false) {
    def dryRunArg = dryRun ? "--dry-run" : ""

    withKubectl(kubeConfigFileCredentialsId, kubectlVersion, {
        sh "kubectl apply -f '$path' $dryRunArg"
    })
}

def kubectlApplyDryRun(kubeConfigFileCredentialsId, kubectlVersion, path) {
    kubectlApply(kubeConfigFileCredentialsId, kubectlVersion, path, true)
}
