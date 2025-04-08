isMasterBranch() {
    return env.BRANCH_NAME == 'master'
        || env.BRANCH_NAME == 'main'
}

isDevelopBranch() {
    return env.BRANCH_NAME == 'develop'
}

isReleaseBranch() {
    env.BRANCH_NAME == 'release'
        || env.BRANCH_NAME.startsWith('release/')
}

isDeploymentBranch() {
    return isMasterBranch()
        || isDevelopBranch()
        || isReleaseBranch()
}
