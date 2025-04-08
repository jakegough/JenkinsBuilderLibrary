def isMasterBranch() {
    return env.BRANCH_NAME == 'master' \
        || env.BRANCH_NAME == 'main'
}

def isDevelopBranch() {
    return env.BRANCH_NAME == 'develop'
}

def isReleaseBranch() {
    env.BRANCH_NAME == 'release' \
        || env.BRANCH_NAME.startsWith('release/')
}

def isDeploymentBranch() {
    return isMasterBranch() \
        || isDevelopBranch() \
        || isReleaseBranch()
}
