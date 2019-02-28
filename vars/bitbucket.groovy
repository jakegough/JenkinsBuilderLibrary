import groovy.transform.Field

def updateBuildStatusInProgress(tokenCredentialsId, username, repository) {
    updateBuildStatus(tokenCredentialsId, username, repository, "INPROGRESS", "Build in progress... cross your fingers...");
}

def updateBuildStatusSuccessful(tokenCredentialsId, username, repository) {
    updateBuildStatus(tokenCredentialsId, username, repository, "SUCCESSFUL", "Build passed :)");
}

def updateBuildStatusFailed(tokenCredentialsId, username, repository) {
    updateBuildStatus(tokenCredentialsId, username, repository, "FAILED", "Build failed :(");
}

def updateBuildStatus(tokenCredentialsId, username, repository, state, description) {
    gitCommitHash = git.getFullCommitHash()
    
    // a lot of help from: https://confluence.atlassian.com/bitbucket/integrate-your-build-system-with-bitbucket-cloud-790790968.html
    postToUrl = "https://api.bitbucket.org/2.0/repositories/${username}/${repository}/commit/${gitCommitHash}/statuses/build"

    bodyJson = \
"""{ 
    "state": "${state}", 
    "key": "${BUILD_ID}", 
    "name": "${JOB_NAME}", 
    "url": "${BUILD_URL}", 
    "description": "${description}" 
}"""

	withCredentials([string(credentialsId: tokenCredentialsId, variable: 'TOKEN')]) {
		def response = httpRequest \
			customHeaders: [[name: 'Authorization', value: "token $TOKEN"]], \
			contentType: 'APPLICATION_JSON', \
			httpMode: 'POST', \
			requestBody: bodyJson, \
			url: postToUrl

		// echo "Status: ${response.status}\nContent: ${response.content}"
	}
}
