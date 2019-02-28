import groovy.transform.Field

@Field gitHubUsername = "missing_gitHubUsername";
@Field gitHubRepository = "missing_gitHubRepository";
@Field gitHubTokenCredentialsId = "missing_gitHubTokenCredentialsId";
@Field bitbucketUsername = "missing_bitbucketUsername";
@Field bitbucketRepository = "missing_bitbucketRepository";
@Field bitbucketTokenCredentialsId = "missing_bitbucketTokenCredentialsId";
@Field nuGetCredentialsId = "missing_nuGetCredentialsId";
@Field nuGetSourceUrl = null;
@Field dockerRegistryCredentialsId = "missing_dockerRegistryCredentialsId";
@Field dockerRegistry = null;

def getAuthor(){
    return git.getAuthor();
}

def getShortGitCommitHash() {
    return git.getShortCommitHash();
}

def getFullGitCommitHash() {
    return git.getFullCommitHash();
}

def updateBitbucketBuildStatusInProgress() {
    bitbucket.updateBuildStatusInProgress(bitbucketTokenCredentialsId, bitbucketUsername, bitbucketRepository);
}

def updateBitbucketBuildStatusSuccessful() {
    bitbucket.updateBuildStatusSuccessful(bitbucketTokenCredentialsId, bitbucketUsername, bitbucketRepository);
}

def updateBitbucketBuildStatusFailed() {
    bitbucket.updateBuildStatusFailed(bitbucketTokenCredentialsId, bitbucketUsername, bitbucketRepository);
}

def updateGitHubBuildStatusInProgress() {
    github.updateBuildStatusInProgress(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository);
}

def updateGitHubBuildStatusSuccessful() {
    github.updateBuildStatusSuccessful(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository);
}

def updateGitHubBuildStatusFailed() {
    github.updateBuildStatusFailed(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository);
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
