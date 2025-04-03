import groovy.transform.Field

def updateBuildStatusInProgress(tokenCredentialsId, username, repository, gitCommitHash = null) {
    updateBuildStatus(tokenCredentialsId, username, repository, "pending", "Build in progress... cross your fingers...", gitCommitHash);
}

def updateBuildStatusSuccessful(tokenCredentialsId, username, repository, gitCommitHash = null) {
    updateBuildStatus(tokenCredentialsId, username, repository, "success", "Build passed :)", gitCommitHash);
}

def updateBuildStatusFailed(tokenCredentialsId, username, repository, gitCommitHash = null) {
    updateBuildStatus(tokenCredentialsId, username, repository, "failure", "Build failed :(", gitCommitHash);
}

def updateBuildStatus(tokenCredentialsId, username, repository, state, description, gitCommitHash = null) {

    def gitCommitHashOrDefault = gitCommitHash ?: git.getFullCommitHash();
    
    // a lot of help from: https://stackoverflow.com/questions/14274293/show-current-state-of-jenkins-build-on-github-repo
    def postToUrl = "https://api.github.com/repos/${username}/${repository}/statuses/${gitCommitHashOrDefault}"

    def bodyJson = \
"""{ 
    "state": "${state}",
    "target_url": "${BUILD_URL}", 
    "description": "${description}" 
}"""

	withCredentials([string(credentialsId: tokenCredentialsId, variable: 'TOKEN')]) {
		def response = httpRequest \
			customHeaders: [[name: 'Authorization', value: "Bearer " + TOKEN]], \
			contentType: 'APPLICATION_JSON', \
			httpMode: 'POST', \
			requestBody: bodyJson, \
			url: postToUrl

		// echo "Status: ${response.status}\nContent: ${response.content}"
	}
}
