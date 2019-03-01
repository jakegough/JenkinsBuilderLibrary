import groovy.transform.Field

@Field kubectlDockerImage="lachlanevenson/k8s-kubectl";

def withKubectl(fileCredentialsId, callback, kubectlVersion = null) {
    kubectlVersionOrDefault = kubectlVersion ?: "latest";

    docker.image("$kubectlDockerImage:$kubectlVersionOrDefault").inside('--entrypoint ""') {
        withCredentials([file(credentialsId: fileCredentialsId, variable: 'KUBECONFIG')]) {
            callback()
        }
    }
}
