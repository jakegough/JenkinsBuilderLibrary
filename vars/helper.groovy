import groovy.transform.Field

@Field gitHubUsername = "missing_gitHubUsername";
@Field gitHubRepository = "missing_gitHubRepository";
@Field gitHubTokenCredentialsId = "missing_gitHubTokenCredentialsId";
@Field nuGetCredentialsId = "missing_nuGetCredentialsId";
@Field nuGetSourceUrl = null;

def getAuthor(){
    return git.getAuthor();
}

def getShortGitCommitHash() {
    return git.getShortCommitHash();
}

def getFullGitCommitHash() {
    return git.getFullCommitHash();
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
    nuget.pushNugetPackage(nupkgDir, credentialsIdOrDefault, sourceUrlOrDefault);
}
