def pluginExists(pluginId) {
    def plugins = jenkins.model.Jenkins.instance.getPluginManager().getPlugins()
    def matchingPluginCount = plugins.filter({ x -> x.getShortName() == pluginId }).count()
    return matchingPluginCount > 0
}

def verifyPluginExists(pluginId) {
    if (!pluginExists(pluginId)) {
        error("Missing plugin: '${pluginId}' (see https://plugins.jenkins.io/${pluginId})")
    }
}
