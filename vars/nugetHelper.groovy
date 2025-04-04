import groovy.transform.Field

@Field nugetDockerImage="mcr.microsoft.com/dotnet/sdk:8.0"; // 8.0 is LTS

def pushPackage(nupkgDir, credentialsId, sourceUrl = null, symbolSourceUrl = null) {

    def sourceUrlOrDefault = sourceUrl ?: "https://api.nuget.org/v3/index.json";

    def nupkgFiles = getNupkgFiles(nupkgDir)

    // requires plugin: https://plugins.jenkins.io/docker-plugin
    pluginHelper.verifyPluginExists('docker-plugin')
    
    try {
        withCredentials([string(credentialsId: credentialsId, variable: "nuget_api_key")]) {
            docker.image(nugetDockerImage).pull()
            docker.image(nugetDockerImage).inside() {
                for(nupkgFile in nupkgFiles){
                    sh """
                        dotnet nuget push '$nupkgFile' --source '$sourceUrlOrDefault' --api-key '$nuget_api_key'
                    """
                }
            }
        }   
    }
    catch(Exception e)  {
        error("NuGet push failed.")
        throw e;
    }
}

def getNupkgFiles(nupkgDir) {
    // symbol packages (.snupkg) are automatically pushed when in the same folder as their corresponding nupkg
    def script = "ls -1 $nupkgDir/*.nupkg | grep -v symbols"
    def stdout = sh(returnStdout: true, script: script).toString()
    def nupkgFiles = stdout.split("\n")

    for (i in 0 ..< nupkgFiles.size()) {
        nupkgFiles[i] = nupkgFiles[i].trim()
    }

    nupkgFiles = nupkgFiles.findAll({ item -> (item) }) // null and empty evaluate to false

    return nupkgFiles
}
