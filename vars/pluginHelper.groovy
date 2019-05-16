def pluginExists(pluginId) {
    def allPlugins = jenkins.model.Jenkins.instance.getPluginManager().getPlugins()
    def matchingPlugin = allPlugins.find { plugin ->  plugin.getShortName() == pluginId }
    return matchingPlugin != null
}

def verifyPluginExists(pluginId) {
    if (!pluginExists(pluginId)) {
        error("Missing plugin: '${pluginId}' (see https://plugins.jenkins.io/${pluginId})")
    }
}
