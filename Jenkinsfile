node('linux && make && docker && kubectl') {
  docker.image('lachlanevenson/k8s-kubectl:v1.13.4').inside { c ->
    sh "echo ${c.id}"
  }
}
