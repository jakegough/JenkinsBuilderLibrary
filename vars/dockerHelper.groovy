def getRegistryImageName(image, credentialsId, registry = null) {
    def registryPrefix = ""

    if (registry) {
        registryPrefix = "$registry/"
    }

    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'docker_registry_user', passwordVariable: 'docker_registry_password')]) {
        return registryPrefix + "$docker_registry_user/$image"
    }
}

def tag(sourceImage, targetImage) {
    sh "docker tag $sourceImage $targetImage"
}

def login(credentialsId, registry = null) {
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'docker_registry_user', passwordVariable: 'docker_registry_password')]) {
        sh "docker login ${registry ?: ''} -u=$docker_registry_user -p=$docker_registry_password"
    }
}

def pushImage(image) {
    try {
        sh "docker push $image"
    }
    catch(Exception e)  {
        error("Docker push failed.")
        throw e;
    }
}

def removeImage(image) {
    sh "docker rmi $image || echo 'image $image not found'"
}

def removeContainer(contaimer) {
    sh "docker rm $contaimer || echo 'contaimer $contaimer not found'"
}

def tagAndPushImageBeta(timestamp, image, credentialsId, registry = null) {
    dockerLogin(credentialsId, registry)

    def dockerRegistryImage = getRegistryImageName(image, credentialsId, registry)                    

    def registryTags = [ 
        "${dockerRegistryImage}:beta", 
        "${dockerRegistryImage}:beta-${timestamp}" 
    ]

    pushDockerFoo(dockerLocalTag, registryTags)
}

def tagAndPushImageRelease(timestamp, image, credentialsId, registry = null) {
    dockerLogin(credentialsId, registry)

    def dockerRegistryImage = getRegistryImageName(image, credentialsId, registry)                    

    def registryTags = [ 
        "${dockerRegistryImage}:latest", 
        "${dockerRegistryImage}:${timestamp}" 
    ]
    
    pushDockerFoo(dockerLocalTag, registryTags)
}

def pushFoo(localTag, registryTags) {    
    for(registryTag in registryTags) {
        tag(localTag, registryTag)
    }

    try {
        for(registryTag in registryTags) {
            pushImage(registryTag)
        }
    }
    catch(Exception e)  {
        error("Docker push failed.")
        throw e;
    }
    finally {
        for(registryTag in registryTags) {
            removeImage(registryTag)
        }
    }
}