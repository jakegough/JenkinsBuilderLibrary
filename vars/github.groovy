import groovy.transform.Field

@Field username = ''
@Field repository = ''
@Field tokenCredentialId = ''

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
    gitCommit = git.getFullCommitHash()
    
    // a lot of help from: https://stackoverflow.com/questions/14274293/show-current-state-of-jenkins-build-on-github-repo
    postToUrl = "https://api.github.com/repos/${username}/${repository}/statuses/${git_commit}"

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
