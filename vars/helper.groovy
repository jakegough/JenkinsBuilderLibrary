import groovy.transform.Field

@Field gitHubUsername = null;
@Field gitHubRepository = null;
@Field gitHubTokenCredentialsId = "missing_gitHubTokenCredentialsId";
@Field bitbucketUsername = null;
@Field bitbucketRepository = null;
@Field bitbucketUserPassCredentialsId = "missing_bitbucketUserPassCredentialsId";
@Field nuGetCredentialsId = "missing_nuGetCredentialsId";
@Field nuGetSourceUrl = null;
@Field dockerRegistryCredentialsId = "missing_dockerRegistryCredentialsId";
@Field dockerRegistry = null;
@Field dockerImageName = null;
@Field kubectlKubeConfigFileCredentialsId = "missing_kubectlKubeConfigFileCredentialsId";
@Field kubectlVersion = null;
@Field cleanWsExcludePattern = null;
@Field xunitTestResultsPattern = null;

def run(nodeLabel, callback) {
  node(nodeLabel) {
    // requires ansiColor plugin: https://wiki.jenkins.io/display/JENKINS/AnsiColor+Plugin
    ansiColor('xterm') {

      stage('Clone') {
        cleanWs()
        checkout scm
      }
      
      def gitCommit = getFullGitCommitHash()    
      
      stage('Start') {
        updateBuildStatusInProgress(gitCommit)
      }

      try
      {
        callback()
      }
      catch(Exception e) {
        updateBuildStatusFailed(gitCommit)
        throw e
      }
      finally {
        if (xunitTestResultsPattern) {
          try {
            // requires xunit plugin: https://plugins.jenkins.io/xunit
            xunit tools: [MSTest(pattern: xunitTestResultsPattern)]
          }
          catch(Exception e) 
          {
          }
        }

        if (cleanWsExcludePattern) {
          cleanWs(deleteDirs: true, patterns: [[type: 'EXCLUDE', pattern: cleanWsExcludePattern]])
        }
        else {
          cleanWs()
        }
      }
      stage('Finish') {
        updateBuildStatusSuccessful(gitCommit)
      }
    }
  }
}

def getTimestamp() {
    if (isUnix()) {
        return sh(returnStdout: true, script: "date +'%Y%m%d%H%M%S'").toString().trim()
    }
    else {
        return bat(returnStdout: true, script: "echo %date:~-4,4%%date:~-10,2%%date:~-7,2%%time:~-11,2%%time:~-8,2%%time:~-5,2%").toString().trim()
    }
}

def getAuthor(){
    return gitHelper.getAuthor();
}

def getShortGitCommitHash() {
    return gitHelper.getShortCommitHash();
}

def getFullGitCommitHash() {
    return gitHelper.getFullCommitHash();
}

def getSafeJobName() {
    return env.JOB_NAME.replaceAll('[^A-Za-z0-9]', '_').toLowerCase();
}

def getFilesChangedInBranch(patterns) {
    return gitHelper.getFilesChangedInBranch(patterns);
}

def getFilesChangedInLastCommit(patterns) {
    return gitHelper.getFilesChangedInLastCommit(patterns);
}

def updateBuildStatusInProgress(gitCommitHash = null) {
    if (gitHubRepository)
    {
        githubHelper.updateBuildStatusInProgress(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
    }
    if (bitbucketRepository)
    {
        bitbucketHelper.updateBuildStatusInProgress(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
    }
}

def updateBuildStatusSuccessful(gitCommitHash = null) {
    if (gitHubRepository)
    {
        githubHelper.updateBuildStatusSuccessful(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
    }
    if (bitbucketRepository)
    {
        bitbucketHelper.updateBuildStatusSuccessful(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
    }
}

def updateBuildStatusFailed(gitCommitHash = null) {
    if (gitHubRepository)
    {
        githubHelper.updateBuildStatusFailed(gitHubTokenCredentialsId, gitHubUsername, gitHubRepository, gitCommitHash);
    }
    if (bitbucketRepository)
    {
        bitbucketHelper.updateBuildStatusFailed(bitbucketUserPassCredentialsId, bitbucketUsername, bitbucketRepository, gitCommitHash);
    }
}

def pushNugetPackage(nupkgDir, credentialsId = null, sourceUrl = null) {
    credentialsIdOrDefault = credentialsId ?: nuGetCredentialsId;
    sourceUrlOrDefault = sourceUrl ?: nuGetSourceUrl;
    nugetHelper.pushPackage(nupkgDir, credentialsIdOrDefault, sourceUrlOrDefault);
}

def getDockerRegistryImageName(image = null, registry = null, credentialsId = null) {
    imageOrDefault = image ?: dockerImageName;
    registryOrDefault = registry ?: dockerRegistry;
    credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;        
    return dockerHelper.getRegistryImageName(imageOrDefault, credentialsIdOrDefault, registryOrDefault)
}

def tagDockerImage(sourceImage, targetImage) {
    dockerHelper.tag(sourceImage, targetImage);
}

def dockerLogin(registry = null, credentialsId = null) {
    registryOrDefault = registry ?: dockerRegistry;
    credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;    
    dockerHelper.login(credentialsIdOrDefault, registryOrDefault);
}

def pushDockerImage(image) {
    dockerHelper.pushImage(image);
}

def removeDockerImage(image) {
    dockerHelper.removeImage(image);
}

def tagAndPushDockerImageBeta(dockerLocalTag, timestamp = null, image = null, credentialsId = null, registry = null) {
    timestampOrDefault = timestamp ?: getTimestamp();
    imageOrDefault = image ?: dockerImageName;
    registryOrDefault = registry ?: dockerRegistry;
    credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;

    dockerHelper.tagAndPushImageBeta(dockerLocalTag, timestampOrDefault, imageOrDefault, credentialsIdOrDefault, registryOrDefault)
}

def tagAndPushDockerImageRelease(dockerLocalTag, timestamp = null, image = null, credentialsId = null, registry = null) {
    timestampOrDefault = timestamp ?: getTimestamp();
    imageOrDefault = image ?: dockerImageName;
    registryOrDefault = registry ?: dockerRegistry;
    credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;

    dockerHelper.tagAndPushImageRelease(dockerLocalTag, timestampOrDefault, imageOrDefault, credentialsIdOrDefault, registryOrDefault)
}

def withKubectl(callback) {
    kubernetesHelper.withKubectl(kubectlKubeConfigFileCredentialsId, kubectlVersion, callback);
}

def kubectlApply(path) {
    kubernetesHelper.kubectlApply(kubectlKubeConfigFileCredentialsId, kubectlVersion, path);
}

def kubectlApplyDryRun(path) {
    kubernetesHelper.kubectlApplyDryRun(kubectlKubeConfigFileCredentialsId, kubectlVersion, path);
}
