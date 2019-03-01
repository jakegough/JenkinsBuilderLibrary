import groovy.transform.Field

def updateBuildStatusInProgress(tokenCredentialsId, username, repository) {
    updateBuildStatus(tokenCredentialsId, username, repository, "pending", "Build in progress... cross your fingers...");
}

def updateBuildStatusSuccessful(tokenCredentialsId, username, repository) {
    updateBuildStatus(tokenCredentialsId, username, repository, "success", "Build passed :)");
}

def updateBuildStatusFailed(tokenCredentialsId, username, repository) {
    updateBuildStatus(tokenCredentialsId, username, repository, "failure", "Build failed :(");
}

def updateBuildStatus(tokenCredentialsId, username, repository, state, description) {
    gitCommitHash = git.getFullCommitHash()
    
    // a lot of help from: https://stackoverflow.com/questions/14274293/show-current-state-of-jenkins-build-on-github-repo
    postToUrl = "https://api.github.com/repos/${username}/${repository}/statuses/${gitCommitHash}"

    bodyJson = \
"""{ 
    "state": "${state}",
    "target_url": "${BUILD_URL}", 
    "description": "${description}" 
}"""

	withCredentials([string(credentialsId: tokenCredentialsId, variable: 'TOKEN')]) {
		def response = httpRequest \
			customHeaders: [[name: 'Authorization', value: "token $TOKEN"]], \
			contentType: 'application/json', \
			httpMode: 'POST', \
			requestBody: bodyJson, \
			url: postToUrl

		// echo "Status: ${response.status}\nContent: ${response.content}"
	}
}
