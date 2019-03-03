import groovy.transform.Field

@Field gitHubUsername = null;
@Field gitHubRepository = null;
@Field gitHubTokenCredentialsId = "missing_gitHubTokenCredentialsId";
@Field bitbucketUsername = null;
@Field bitbucketRepository = null;
@Field bitbucketUserPassCredentialsId = "missing_bitbucketUserPassCredentialsId";
@Field nuGetCredentialsId = "missing_nuGetCredentialsId";
@Field nuGetSourceUrl = null;
@Field dockerRegistryCredentialsId = "missing_dockerRegistryCredentialsId";
@Field dockerRegistry = null;
@Field dockerImageName = null;
@Field kubectlKubeConfigFileCredentialsId = "missing_kubectlKubeConfigFileCredentialsId";
@Field kubectlVersion = null;
@Field cleanWsExcludePattern = null;

def run(nodeLabel, callback) {
  node(nodeLabel) {
    stage('Clone') {
      cleanWs()
      checkout scm
    }
    
    def gitCommit = getFullGitCommitHash()    
    
    stage('Start') {
      updateBuildStatusInProgress(gitCommit)
    }

    try
    {
      callback()
    }
    catch(Exception e) {
      updateBuildStatusFailed(gitCommit)
      throw e
    }
    finally {
        if (cleanWsExcludePattern) {
            cleanWs(deleteDirs: true, patterns: [[type: 'EXCLUDE', pattern: cleanWsExcludePattern]])
        }
        else {
            cleanWs()
        }      
    }
    stage('Finish') {
      updateBuildStatusSuccessful(gitCommit)
    }    
  }
}

def getTimestamp() {
    if (isUnix()) {
        return sh(returnStdout: true, script: "date +'%Y%m%d%H%M%S'").toString().trim()
    }
    else {
        return bat(returnStdout: true, script: "echo %date:~-4,4%%date:~-10,2%%date:~-7,2%%time:~-11,2%%time:~-8,2%%time:~-5,2%").toString().trim()
    }
}

def getAuthor(){
    return gitHelper.getAuthor();
}

def getShortGitCommitHash() {
    return gitHelper.getShortCommitHash();
}

def getFullGitCommitHash() {
    return gitHelper.getFullCommitHash();
}

def getFilesChangedInBranch(patterns) {
    return gitHelper.getFilesChangedInBranch(patterns);
}

def getFilesChangedInLastCommit(patterns) {
    return gitHelper.getFilesChangedInLastCommit(patterns);
}

def updateBuildStatusInProgress(gitCommitHash = null) {
    if (gitHubRepository)
    {
        updateGitHubBuildStatusInProgress(gitCommitHash)
    }
    if (bitbucketRepository)
    {
        updateBitbucketBuildStatusInProgress(gitCommitHash)
    }
}

def updateBuildStatusSuccessful(gitCommitHash = null) {
    if (gitHubRepository)
    {
        updateGitHubBuildStatusSuccessful(gitCommitHash)
    }
    if (bitbucketRepository)
    {
        updateBitbucketBuildStatusSuccessful(gitCommitHash)
    }
}

def updateBuildStatusFailed(gitCommitHash = null) {
    if (gitHubRepository)
    {
        updateGitHubBuildStatusFailed(gitCommitHash)
    }
    if (bitbucketRepository)
    {
        updateBitbucketBuildStatusFailed(gitCommitHash)
    }
}

def updateBitbucketBuildStatusInProgress(gitCommitHash = null) {
    bitbucketHelper.updateBuildStatusInProgress(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
}

def updateBitbucketBuildStatusSuccessful(gitCommitHash = null) {
    bitbucketHelper.updateBuildStatusSuccessful(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
}

def updateBitbucketBuildStatusFailed(gitCommitHash = null) {
    bitbucketHelper.updateBuildStatusFailed(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
}

def updateGitHubBuildStatusInProgress(gitCommitHash = null) {
    githubHelper.updateBuildStatusInProgress(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
}

def updateGitHubBuildStatusSuccessful(gitCommitHash = null) {
    githubHelper.updateBuildStatusSuccessful(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
}

def updateGitHubBuildStatusFailed(gitCommitHash = null) {
    githubHelper.updateBuildStatusFailed(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
}

def pushNugetPackage(nupkgDir, credentialsId = null, sourceUrl = null) {
    credentialsIdOrDefault = credentialsId ?: nuGetCredentialsId;
    sourceUrlOrDefault = sourceUrl ?: nuGetSourceUrl;
    nugetHelper.pushPackage(nupkgDir, credentialsIdOrDefault, sourceUrlOrDefault);
}

def getDockerRegistryImageName(image = null, registry = null, credentialsId = null) {
    imageOrDefault = image ?: dockerImageName;
    registryOrDefault = registry ?: dockerRegistry;
    credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;        
    return dockerHelper.getRegistryImageName(imageOrDefault, credentialsIdOrDefault, registryOrDefault)
}

def tagDockerImage(sourceImage, targetImage) {
    dockerHelper.tag(sourceImage, targetImage);
}

def dockerLogin(registry = null, credentialsId = null) {
    registryOrDefault = registry ?: dockerRegistry;
    credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;    
    dockerHelper.login(credentialsIdOrDefault, registryOrDefault);
}

def pushDockerImage(image) {
    dockerHelper.pushImage(image, credentialsIdOrDefault, registryOrDefault);
}

def removeDockerImage(image) {
    dockerHelper.removeImage(image);
}

def withKubectl(callback) {
    kubernetesHelper.withKubectl(kubectlKubeConfigFileCredentialsId, kubectlVersion, callback);
}

def kubectlApply(path) {
    kubernetesHelper.kubectlApply(kubectlKubeConfigFileCredentialsId, kubectlVersion, path);
}

def kubectlApplyDryRun(path) {
    kubernetesHelper.kubectlApplyDryRun(kubectlKubeConfigFileCredentialsId, kubectlVersion, path);
}
