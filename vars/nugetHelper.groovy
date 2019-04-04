import groovy.transform.Field

@Field nugetDockerImage="microsoft/dotnet:2.0-sdk";

def pushPackage(nupkgDir, credentialsId, sourceUrl = null) {

    sourceUrlOrDefault = sourceUrl ?: "https://www.nuget.org/api/v2/package";

    withCredentials([string(credentialsId: credentialsId, variable: "nuget_api_key")]) {
        docker.image(nugetDockerImage).inside("-u root") {
            // TODO: maybe some day I'll care about symbols... see also
            // --symbol-source
            // --symbol-api-key
            // --no-symbols
            // def symbolsFile = sh(returnStdout: true, script: "ls -1 $nupkgDir/*.nupkg | grep symbols").toString().trim();

            def nupkgFiles = getNupkgFiles(nupkgDir)

            for(nupkgFile in nupkgFiles){
                sh """
                    dotnet nuget push '$nupkgFile' --source '$sourceUrlOrDefault' --api-key '$nuget_api_key'
                """
            }
        }
    }
}

def getNupkgFiles(nupkgDir) {

    script = "ls -1 $nupkgDir/*.nupkg | grep -v symbols"
    stdout = sh(returnStdout: true, script: script).toString()
    nupkgFiles = stdout.split("\n")

    for (i in 0 ..< nupkgFiles.size()) {
        nupkgFiles[i] = nupkgFiles[i].trim()
    }

    nupkgFiles = nupkgFiles.findAll({ item -> !(item) }) // null and empty evaluate to false

    return nupkgFiles
}
