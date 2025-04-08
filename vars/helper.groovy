import groovy.transform.Field

@Field gitHubUsername = null;
@Field gitHubRepository = null;
@Field gitHubTokenCredentialsId = "missing_gitHubTokenCredentialsId";
@Field bitbucketUsername = null;
@Field bitbucketRepository = null;
@Field bitbucketUserPassCredentialsId = "missing_bitbucketUserPassCredentialsId";
@Field dockerRegistryCredentialsId = "missing_dockerRegistryCredentialsId";
@Field dockerRegistry = null;
@Field dockerImageName = null;
@Field kubectlKubeConfigFileCredentialsId = "missing_kubectlKubeConfigFileCredentialsId";
@Field kubectlVersion = null;
@Field cleanWsExcludePattern = null;
@Field xunitTestResultsPattern = null;
@Field coberturaCoverageReport = null;
@Field htmlCoverageReportDir = null;
@Field htmlCoverageReportIndexFile = null;

def run(nodeLabel, callback) {
  def isDeployBranch = (
    env.BRANCH_NAME == 'master' ||
    env.BRANCH_NAME == 'main' ||
    env.BRANCH_NAME == 'release' ||
    env.BRANCH_NAME == 'develop' ||
    env.BRANCH_NAME.startsWith('release/')
  )
  // taken from https://www.jvt.me/posts/2020/02/23/jenkins-multibranch-skip-branch-index/
  def isBranchIndexingBuild = currentBuild.getBuildCauses().toString().contains('BranchIndexingCause')
  if (isBranchIndexingBuild && isDeployBranch) {
    print "INFO: Build skipped due to trigger being Branch Indexing"
    currentBuild.result = 'ABORTED'
    return
  }

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
        // TODO: look into post{...} and always{...} blocks
        if (xunitTestResultsPattern) {
          // requires plugin: https://plugins.jenkins.io/
          verifyPluginExists("xunit")

          try {
            // see also: https://jenkins.io/doc/pipeline/steps/xunit/
            xunit tools: [MSTest(pattern: xunitTestResultsPattern)]
          }
          catch(Exception e)
          {
          }
        }

        if (coberturaCoverageReport) {
          // requires plugin: https://plugins.jenkins.io/coverage
          verifyPluginExists("coverage")

          try {
            recordCoverage tools: [[parser: 'COBERTURA', pattern: coberturaCoverageReport]]
          }
          catch(Exception e)
          {
          }
        }

        if (htmlCoverageReportDir) {
          def htmlCoverageReportIndexFileOrDefault = htmlCoverageReportIndexFile ?: "index.htm";

          // requires plugin: https://plugins.jenkins.io/htmlpublisher
          verifyPluginExists("htmlpublisher")

          try {
            // see also: https://jenkins.io/doc/pipeline/steps/htmlpublisher/
            publishHTML target: [
              allowMissing: true,
              alwaysLinkToLastBuild: false,
              keepAll: true,
              reportDir: htmlCoverageReportDir,
              reportFiles: htmlCoverageReportIndexFileOrDefault,
              reportName: "Test Coverage"
            ]
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

def getDockerRegistryImageName(image = null, registry = null, credentialsId = null) {
    def imageOrDefault = image ?: dockerImageName;
    def registryOrDefault = registry ?: dockerRegistry;
    def credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;
    return dockerHelper.getRegistryImageName(imageOrDefault, credentialsIdOrDefault, registryOrDefault)
}

def tagDockerImage(sourceImage, targetImage) {
    dockerHelper.tag(sourceImage, targetImage);
}

def dockerLogin(registry = null, credentialsId = null) {
    def registryOrDefault = registry ?: dockerRegistry;
    def credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;
    dockerHelper.login(credentialsIdOrDefault, registryOrDefault);
}

def pushDockerImage(image) {
    dockerHelper.pushImage(image);
}

def removeDockerImage(image) {
    dockerHelper.removeImage(image);
}

def tagAndPushDockerImageBeta(dockerLocalTag, timestamp = null, image = null, credentialsId = null, registry = null) {
    def timestampOrDefault = timestamp ?: getTimestamp();
    def imageOrDefault = image ?: dockerImageName;
    def registryOrDefault = registry ?: dockerRegistry;
    def credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;

    dockerHelper.tagAndPushImageBeta(dockerLocalTag, timestampOrDefault, imageOrDefault, credentialsIdOrDefault, registryOrDefault)
}

def tagAndPushDockerImageRelease(dockerLocalTag, timestamp = null, image = null, credentialsId = null, registry = null) {
    def timestampOrDefault = timestamp ?: getTimestamp();
    def imageOrDefault = image ?: dockerImageName;
    def registryOrDefault = registry ?: dockerRegistry;
    def credentialsIdOrDefault = credentialsId ?: dockerRegistryCredentialsId;

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

def verifyPluginExists(pluginId) {
    pluginHelper.verifyPluginExists(pluginId);
}

def pluginExists(pluginId) {
    return pluginHelper.pluginExists(pluginId);
}
