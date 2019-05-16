def pluginExists(pluginId) {
    def allPlugins = jenkins.model.Jenkins.instance.getPluginManager().getPlugins()
    
    return allPlugins.find { plugin ->  
        plugin.getShortName() == pluginId 
    }
}

def verifyPluginExists(pluginId) {
    if (!pluginExists(pluginId)) {
        error("Missing plugin: '${pluginId}' (see https://plugins.jenkins.io/${pluginId})")
    }
}
