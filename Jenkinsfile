node('linux && make && docker') {
  docker.image('lachlanevenson/k8s-kubectl:v1.13.4') {
    withCredentials([file(credentialsId: 'k8s-digitalocean', variable: 'KUBECONFIG')]) {
      sh 'kubectl version'
    }
  }
}
