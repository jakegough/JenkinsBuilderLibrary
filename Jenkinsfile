node('linux && make && docker && kubectl') {
  withKubeConfig([credentialsId: 'k8s-digitalocean']) {
    sh 'kubectl version'
  }
}
