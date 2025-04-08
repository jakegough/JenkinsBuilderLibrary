library 'JenkinsBuilderLibrary'

testWithNode('linux', {
    assert helper.getTimestamp() != null

    assert helper.pluginExists('credentials') == true
    assert helper.pluginExists('fizzbuz123') == false
    helper.verifyPluginExists('credentials')

    try {
        helper.verifyPluginExists('fizzbuz123')
        // this should never happen
        error("helper.verifyPluginExists did not throw an exception when it should have")
    }
    catch(Exception e) {  }
})

testWithNode('windows', {
    assert helper.getTimestamp() != null

    assert helper.pluginExists('credentials') == true
    assert helper.pluginExists('fizzbuz123') == false
    helper.verifyPluginExists('credentials')

    try {
        helper.verifyPluginExists('fizzbuz123')
        // this should never happen
        error("helper.verifyPluginExists did not throw an exception when it should have")
    }
    catch(Exception e) {  }
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
