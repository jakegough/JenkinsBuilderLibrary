import groovy.transform.Field

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
    github.updateBuildStatusInProgress();
}

def updateGitHubBuildStatusSuccessful() {
    github.updateBuildStatusSuccessful();
}

def updateGitHubBuildStatusFailed() {
    github.updateBuildStatusFailed();
}
