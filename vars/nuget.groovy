import groovy.transform.Field

@Field nugetDockerImage="microsoft/dotnet:2.0-sdk";

def pushNugetPackage(nupkgDir, credentialsId, sourceUrl = null) {

    sourceUrlOrDefault = sourceUrl ?: "https://www.nuget.org/api/v2/package";

    withCredentials([string(credentialsId: credentialsId, variable: "nuget_api_key")]) {
        docker.image(nugetDockerImage).inside("-u root") {
            // TODO: maybe some day I'll care about symbols... see also
            // --symbol-source
            // --symbol-api-key
            // --no-symbols
            // def symbolsFile = sh(returnStdout: true, script: "ls -1 $nupkgDir/*.nupkg | grep symbols").toString().trim();

            def nupkgFile = sh(returnStdout: true, script: "ls -1 $nupkgDir/*.nupkg | grep -v symbols").toString().trim();

            sh """
                dotnet nuget push '$nupkgFile' --source '$sourceUrlOrDefault' --api-key '$nuget_api_key'
            """
        }
    }
}