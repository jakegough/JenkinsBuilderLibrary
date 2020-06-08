import groovy.transform.Field

@Field nugetDockerImage="microsoft/dotnet:2.1-sdk"; // 2.1 is LTS
@Field nugetCheckDockerImage="jakegough/jaytwo.nugetcheck:20190406015844"; // known version with known syntax

def pushPackage(nupkgDir, credentialsId, sourceUrl = null) {

    sourceUrlOrDefault = sourceUrl ?: "https://www.nuget.org/api/v2/package";

    def nupkgFiles = getNupkgFiles(nupkgDir)

    // requires plugin: https://plugins.jenkins.io/docker-plugin
    pluginHelper.verifyPluginExists('docker-plugin')

    try {
        docker.image(nugetCheckDockerImage).pull()
        docker.image(nugetCheckDockerImage).inside("--entrypoint=''") {
            for(nupkgFile in nupkgFiles) {
                // -gte            lists versions greater than or equal to the version specified
                // --same-major    lists versions only with the same major version as the version specified
                // --opposite-day  fails when results are found

                sh """
                    nugetcheck '$nupkgFile' -gte '$nupkgFile' --same-major --opposite-day
                """
            }
        }
    }
    catch(Exception e)  {
        error("NuGet push failed. NuGet package version must be greater than the latest published package of the same major version.")
        throw e;
    }
    
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
    // symbol packages (.snupkg) can be pushed just like regular packages
    script = "ls -1 $nupkgDir/*.nupkg $nupkgDir/*.snupkg | grep -v symbols"
    stdout = sh(returnStdout: true, script: script).toString()
    nupkgFiles = stdout.split("\n")

    for (i in 0 ..< nupkgFiles.size()) {
        nupkgFiles[i] = nupkgFiles[i].trim()
    }

    nupkgFiles = nupkgFiles.findAll({ item -> (item) }) // null and empty evaluate to false

    return nupkgFiles
}
