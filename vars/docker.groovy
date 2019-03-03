def tag(sourceImage, targetImage) {
    sh "docker tag $sourceImage $targetImage"
}

def login(credentialsId, registry = null) {
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'docker_registry_user', passwordVariable: 'docker_registry_password')]) {
        sh "docker login $registry -u=$docker_registry_user -p=$docker_registry_password"
    }
}

def push(image) {
    sh "docker push $image"
}

def removeImage(image) {
    sh "docker rmi $image"
}
