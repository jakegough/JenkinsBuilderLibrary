node('linux && make && docker && kubectl') {
  withCredentials([file(credentialsId: 'k8s-digitalocean', variable: 'KUBECONFIG')]) {
    sh 'kubectl version'
  }
}
