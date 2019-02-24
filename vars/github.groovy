import groovy.transform.Field

@Field username = "missing_github_username"
@Field repository = "missing_github_repository"
@Field tokenCredentialId = "missing_github_tokenCredentialId"

def updateBuildStatusInProgress() {
    updateBuildStatus("pending", "Build in progress... cross your fingers...");
}

def updateBuildStatusSuccessful() {
    updateBuildStatus("success", "Build passed :)");
}

def updateBuildStatusFailed() {
    updateBuildStatus("failure", "Build failed :(");
}

def updateBuildStatus(state, description) {
    gitCommitHash = git.getFullCommitHash()
    
    // a lot of help from: https://stackoverflow.com/questions/14274293/show-current-state-of-jenkins-build-on-github-repo
    postToUrl = "https://api.github.com/repos/${username}/${repository}/statuses/${gitCommitHash}"

    bodyJson = \
"""{ 
    "state": "${state}",
    "target_url": "${BUILD_URL}", 
    "description": "${description}" 
}"""

	withCredentials([string(credentialsId: tokenCredentialId, variable: 'TOKEN')]) {
		def response = httpRequest \
			customHeaders: [[name: 'Authorization', value: "token $TOKEN"]], \
			contentType: 'APPLICATION_JSON', \
			httpMode: 'POST', \
			requestBody: bodyJson, \
			url: postToUrl

		// echo "Status: ${response.status}\nContent: ${response.content}"
	}
}
