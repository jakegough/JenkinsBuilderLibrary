def inside(String clusterId, Closure callback) {
    def clusterSpec = getClusterSpec(clusterId)
    if (!clusterSpec) {
        error "No clusterSpec found for clusterId: ${clusterId}"
    }

    pluginHelper.verifyPluginExists('docker-plugin')

    def image = docker.image(clusterSpec.kubectlImage)
    image.pull()
    image.inside('--entrypoint ""') {
        withCredentials([file(credentialsId: clusterSpec.kubeConfigFileCredentialsId, variable: 'KUBECONFIG')]) {
            callback()
        }
    }
}

def getClusterSpec(String clusterId) {
    def clusterSpecs = [
        "k3s-general": [
            kubeConfigFileCredentialsId: 'k3s-general-kubeconfig',
            kubectlImage: 'bitnami/kubectl:1.33.1'
        ]
    ]

    return clusterSpecs[clusterId];
}
