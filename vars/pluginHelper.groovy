def pluginExists(pluginId) {
    jenkins.model.Jenkins.instance.getPluginManager().getPlugins().each{ plugin -> 
        if (plugin.getInfo().sourceId == pluginId) {
            return true
        }
    }

    return false
}

def verifyPluginExists(pluginId) {
    if (!pluginExists(pluginId)) {
        error("Missing plugin: '${pluginId}' (see https://plugins.jenkins.io/${pluginId})")
    }
}
