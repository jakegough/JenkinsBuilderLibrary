import groovy.transform.Field

@Field gitHubUsername = "missing_gitHubUsername";
@Field gitHubRepository = "missing_gitHubRepository";
@Field gitHubTokenCredentialsId = "missing_gitHubTokenCredentialsId";
@Field bitbucketUsername = "missing_bitbucketUsername";
@Field bitbucketRepository = "missing_bitbucketRepository";
@Field bitbucketUserPassCredentialsId = "missing_bitbucketUserPassCredentialsId";
@Field nuGetCredentialsId = "missing_nuGetCredentialsId";
@Field nuGetSourceUrl = null;
@Field dockerRegistryCredentialsId = "missing_dockerRegistryCredentialsId";
@Field dockerRegistry = null;
@Field kubectlFileCredentialsId = "missing_kubectlFileCredentialsId";
@Field kubectlVersion = null;
@Field slackBotTokenCredentialsId = "missing_slackBotTokenCredentialsId";
@Field slackChannel = "missing_slackChannel";
@Field slackBotDisplayName = "missing_slackBotDisplayName";

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
    bitbucket.updateBuildStatusInProgress(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository);
}

def updateBitbucketBuildStatusSuccessful() {
    bitbucket.updateBuildStatusSuccessful(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository);
}

def updateBitbucketBuildStatusFailed() {
    bitbucket.updateBuildStatusFailed(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository);
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

def withKubectl(callback) {
    kubectl.run(kubectlFileCredentialsId, callback, kubectlVersion);
}

def postSlackMessage(text) {
    kubectl.postMessage(slackBotTokenCredentialsId, slackChannel, slackBotDisplayName, text);
}
