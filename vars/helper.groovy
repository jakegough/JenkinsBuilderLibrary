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

def getAuthor(){
    return git.getAuthor();
}

def getShortGitCommitHash() {
    return git.getShortCommitHash();
}

def getFullGitCommitHash() {
    return git.getFullCommitHash();
}

def getFilesChangedInBranch(patterns) {
    return git.getFilesChangedInBranch(patterns);
}

def getFilesChangedInLastCommit(patterns) {
    return git.getFilesChangedInLastCommit(patterns);
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
    bitbucket.updateBuildStatusInProgress(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
}

def updateBitbucketBuildStatusSuccessful(gitCommitHash = null) {
    bitbucket.updateBuildStatusSuccessful(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
}

def updateBitbucketBuildStatusFailed(gitCommitHash = null) {
    bitbucket.updateBuildStatusFailed(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
}

def updateGitHubBuildStatusInProgress(gitCommitHash = null) {
    github.updateBuildStatusInProgress(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
}

def updateGitHubBuildStatusSuccessful(gitCommitHash = null) {
    github.updateBuildStatusSuccessful(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
}

def updateGitHubBuildStatusFailed(gitCommitHash = null) {
    github.updateBuildStatusFailed(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
}

def pushNugetPackage(nupkgDir, credentialsId = null, sourceUrl = null) {
    credentialsIdOrDefault = credentialsId ?: nuGetCredentialsId;
    sourceUrlOrDefault = sourceUrl ?: nuGetSourceUrl;
    nuget.pushPackage(nupkgDir, credentialsIdOrDefault, sourceUrlOrDefault);
}

def pushDockerImage(localImage, registryImage, credentialsId = null, registry = null) {
    credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;
    registryOrDefault = registry ?: dockerRegistry;
    docker.pushImage(imageName, originalTagName, newTagName, credentialsIdOrDefault, registryOrDefault);
}

def tagDockerImage(sourceImage, targetImage) {
    docker.tag(sourceImage, targetImage);
}

def withKubectl(callback) {
    kubernetes.withKubectl(kubectlKubeConfigFileCredentialsId, kubectlVersion, callback);
}

def kubectlApply(path) {
    kubernetes.kubectlApply(kubectlKubeConfigFileCredentialsId, kubectlVersion, path);
}

def kubectlApplyDryRun(path) {
    kubernetes.kubectlApplyDryRun(kubectlKubeConfigFileCredentialsId, kubectlVersion, path);
}
