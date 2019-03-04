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

def getFilesChangedInBranch(patterns) {
    return getFilesChanged(patterns, "origin/master")
}

def getFilesChangedInLastCommit(patterns) {
    return getFilesChanged(patterns, "HEAD~1")
}

def getFilesChanged(patterns, diffWith) {
    def changedFiles = ""
    for (pattern in patterns) {
        if (isUnix()) {
            changedFiles += sh(returnStdout: true, script: "git diff $diffWith --name-only --diff-filter=AMCR -- $pattern").toString().trim()
        }
        else {
            changedFiles += bat(returnStdout: true, script: "@git diff $diffWith --name-only --diff-filter=AMCR -- $pattern").toString().trim()
        }
    }
    return changedFiles.trim()
}
