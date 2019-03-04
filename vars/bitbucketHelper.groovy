import groovy.transform.Field

def updateBuildStatusInProgress(userPassCredentialsId, username, repository, gitCommitHash = null) {
    updateBuildStatus(userPassCredentialsId, username, repository, "INPROGRESS", "Build in progress... cross your fingers...", gitCommitHash);
}

def updateBuildStatusSuccessful(userPassCredentialsId, username, repository, gitCommitHash = null) {
    updateBuildStatus(userPassCredentialsId, username, repository, "SUCCESSFUL", "Build passed :)", gitCommitHash);
}

def updateBuildStatusFailed(userPassCredentialsId, username, repository, gitCommitHash = null) {
    updateBuildStatus(userPassCredentialsId, username, repository, "FAILED", "Build failed :(", gitCommitHash);
}

def updateBuildStatus(userPassCredentialsId, username, repository, state, description, gitCommitHash = null) {
    gitCommitHashOrDefault = gitCommitHash ?: git.getFullCommitHash();
    
    // a lot of help from: https://confluence.atlassian.com/bitbucket/integrate-your-build-system-with-bitbucket-cloud-790790968.html
    postToUrl = "https://api.bitbucket.org/2.0/repositories/${username}/${repository}/commit/${gitCommitHashOrDefault}/statuses/build"

    bodyJson = \
"""{ 
    "state": "${state}", 
    "key": "${BUILD_ID}", 
    "name": "${JOB_NAME}", 
    "url": "${BUILD_URL}", 
    "description": "${description}" 
}"""

	def response = httpRequest \
        authentication: userPassCredentialsId, \
        contentType: 'application/json', \
        httpMode: 'POST', \
        requestBody: bodyJson, \
        url: postToUrl

    // echo "Status: ${response.status}\nContent: ${response.content}"
}
