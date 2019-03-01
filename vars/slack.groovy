def postMessage(botTokenCredentialsId, channel, username, text) {
    // https://api.slack.com/methods/chat.postMessage
    postToUrl = "https://slack.com/api/chat.postMessage"

    bodyJson = \
"""{ 
    "channel": "${channel}",
    "text": "${text}",
    "as_user ": false, 
    "username": "${username}" 
}"""

	withCredentials([string(credentialsId: botTokenCredentialsId, variable: 'TOKEN')]) {
		def response = httpRequest \
			customHeaders: [[name: 'Authorization', value: "Bearer $TOKEN"]], \
			contentType: 'application/json', \
			httpMode: 'POST', \
			requestBody: bodyJson, \
			url: postToUrl

		// echo "Status: ${response.status}\nContent: ${response.content}"
	}
}
