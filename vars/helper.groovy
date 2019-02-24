import groovy.transform.Field

@Field gitHubUsername = "missing_github_username"
@Field gitHubRepository = "missing_github_repository"
@Field gitHubTokenCredentialId = "missing_github_tokenCredentialId"

def getAuthor(){
    return git.getAuthor()
}

def getShortGitCommitHash() {
    return git.getShortCommitHash()
}

def getFullGitCommitHash() {
    return git.getFullCommitHash()
}

def updateGitHubBuildStatusInProgress() {
    github.updateBuildStatusInProgress(gitHubUsername, gitHubRepository, gitHubTokenCredentialId);
}

def updateGitHubBuildStatusSuccessful() {
    github.updateBuildStatusSuccessful(gitHubUsername, gitHubRepository, gitHubTokenCredentialId);
}

def updateGitHubBuildStatusFailed() {
    github.updateBuildStatusFailed(gitHubUsername, gitHubRepository, gitHubTokenCredentialId);
}
