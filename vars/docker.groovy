def pushImage(localImage, registryImage, credentialsId, registryUrl = null) {
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'docker_registry_user', passwordVariable: 'docker_registry_password')]) {
        sh "docker login $registryUrl -u=$docker_registry_user -p=$docker_registry_password"
    }

    registryPrefix = ''
    if (registry != null) {
        registryPrefix = "$registryUrl/"
    }

    sh "docker tag $localImage ${registryPrefix}$registryImage"
    sh "docker push ${registryPrefix}$registryImage"
}

def tag(sourceImage, targetImage) {
    sh "docker tag $sourceImage $targetImage"
}
