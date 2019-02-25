import groovy.transform.Field

@Field nugetDockerImage="microsoft/dotnet:2.0-sdk"

def pushNugetPackage(nupkgFile, credentialsId, sourceUrl = null) {    
    sourceArg = ''
    if (sourceUrl != null) {
        sourceArg = "--source '$sourceUrl'"
    }

    withCredentials([string(credentialsId: credentialsId, variable: "nuget_api_key")]) {
        docker.image(nugetDockerImage).inside("-u root") {
            sh """
                dotnet nuget push '$nupkgFile' --api-key '$nuget_api_key' $sourceArg
            """
        }
    }
}