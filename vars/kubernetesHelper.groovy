import groovy.transform.Field

@Field kubectlDockerImage="lachlanevenson/k8s-kubectl";

def withKubectl(kubeConfigFileCredentialsId, kubectlVersion, callback) {
    kubectlVersionOrDefault = kubectlVersion ?: "latest";

    docker.image("$kubectlDockerImage:$kubectlVersionOrDefault").inside('--entrypoint ""') {
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
