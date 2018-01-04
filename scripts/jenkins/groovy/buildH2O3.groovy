def call(final pipelineContext) {

  def PYTHON_VERSION = '3.5'
  def R_VERSION = '3.4.1'

  // Load required scripts
  def insideDocker = load('h2o-3/scripts/jenkins/groovy/insideDocker.groovy')
  def buildTarget = load('h2o-3/scripts/jenkins/groovy/buildTarget.groovy')
  def customEnv = load('h2o-3/scripts/jenkins/groovy/customEnv.groovy')

  def stageName = 'Build H2O-3'
  
  withCustomCommitStates(scm, pipelineContext.getBuildConfig().H2O_OPS_TOKEN, "${pipelineContext.getBuildConfig().getGitHubCommitStateContext(stageName)}") {
    pipelineContext.getBuildSummary().addStageSummary(this, stageName)
    pipelineContext.getBuildSummary().setStageDetails(this, stageName, env.NODE_NAME, env.WORKSPACE)
    try {
      // Launch docker container, build h2o-3, create test packages and archive artifacts
      def buildEnv = customEnv() + "PYTHON_VERSION=${PYTHON_VERSION}" + "R_VERSION=${R_VERSION}"
      insideDocker(buildEnv, pipelineContext.getBuildConfig().DEFAULT_IMAGE, pipelineContext.getBuildConfig().DOCKER_REGISTRY, 30, 'MINUTES') {
        stage(stageName) {
          try {
            buildTarget {
              target = 'build-h2o-3'
              hasJUnit = false
              archiveFiles = false
              makefilePath = pipelineContext.getBuildConfig().MAKEFILE_PATH
            }
            buildTarget {
              target = 'test-package-py'
              hasJUnit = false
              archiveFiles = false
              makefilePath = pipelineContext.getBuildConfig().MAKEFILE_PATH
            }
            buildTarget {
              target = 'test-package-r'
              hasJUnit = false
              archiveFiles = false
              makefilePath = pipelineContext.getBuildConfig().MAKEFILE_PATH
            }
            if (pipelineContext.getBuildConfig().langChanged(pipelineContext.getBuildConfig().LANG_JS)) {
              buildTarget {
                target = 'test-package-js'
                hasJUnit = false
                archiveFiles = false
                makefilePath = pipelineContext.getBuildConfig().MAKEFILE_PATH
              }
            }
            if (pipelineContext.getBuildConfig().langChanged(pipelineContext.getBuildConfig().LANG_JAVA)) {
              buildTarget {
                target = 'test-package-java'
                hasJUnit = false
                archiveFiles = false
                makefilePath = pipelineContext.getBuildConfig().MAKEFILE_PATH
              }
            }
          } finally {
            archiveArtifacts """
          h2o-3/${pipelineContext.getBuildConfig().MAKEFILE_PATH},
          h2o-3/h2o-py/dist/*.whl,
          h2o-3/build/h2o.jar,
          h2o-3/h2o-3/src/contrib/h2o_*.tar.gz,
          h2o-3/h2o-assemblies/genmodel/build/libs/genmodel.jar,
          h2o-3/test-package-*.zip,
          **/*.log, **/out.*, **/*py.out.txt, **/java*out.txt, **/tests.txt, **/status.*
        """
          }
        }
      }
      pipelineContext.getBuildSummary().markStageSuccessful(this, stageName)
    } catch (Exception e) {
      pipelineContext.getBuildSummary().markStageFailed(this, stageName)
      throw e
    }
  }
}

return this
