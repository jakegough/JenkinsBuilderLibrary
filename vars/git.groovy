def getAuthor() {
    if (isUnix()) {
        return sh(returnStdout: true, script: "git show --quiet --format=%aN HEAD").toString().trim()
    }
    else {
        return bat(returnStdout: true, script: "@git show --quiet --format=%%aN HEAD").toString().trim()
    }
}

def getShortCommitHash() {
    return getFullCommitHash().take(7)
}

def getFullCommitHash() {
    if (isUnix()) {
        return sh(returnStdout: true, script: "git rev-parse HEAD").toString().trim()
    }
    else {
        return bat(returnStdout: true, script: "@git rev-parse HEAD").toString().trim()
    }
}