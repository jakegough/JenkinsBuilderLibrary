library 'JenkinsBuilderLibrary'

testWithNode('linux', {
    assert helper.getTimestamp() != null     
})

testWithNode('windows', {
    assert helper.getTimestamp() != null     
})

//testWithNode('linux && docker', {
//    helper.withKubectl {
//        sh 'kubectl --help'
//    }
//})

def testWithNode(nodeLabel, callback) {
    node(nodeLabel) {
        stage(nodeLabel) {
            callback()
        }        
    }
}
