def call(body) {
  def config = [:]

  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  config.archiveFiles = config.archiveFiles ?: true
  config.hasJUnit = config.hasJUnit ?: true
  config.h2o3dir = config.h2o3dir ?: 'h2o-3'

  def FILES_TO_ARCHIVE = [
    "**/*.log", "**/out.*", "**/*py.out.txt", "**/java*out.txt", "**/*ipynb.out.txt",
    "**/results/*", "**/*tmp_model*",
    "**/h2o-py/tests/testdir_dynamic_tests/testdir_algos/glm/Rsandbox*/*.csv",
    "**/tests.txt", "**/*lib_h2o-flow_build_js_headless-test.js.out.txt",
    "**/*.code", "**/package_version_check_out.txt"
  ]

  try {
    execMake(config.makefilePath, config.target, config.h2o3dir)
  } finally {
    if (config.hasJUnit) {
      GString findCmd = "find ${config.h2o3dir} -type f -name '*.xml'"
      GString replaceCmd = "${findCmd} -exec sed -i 's/&#[0-9]\\+;//g' {} +"
      echo "Post-processing following test result files:"
      sh findCmd
      sh replaceCmd
      junit testResults: "${config.h2o3dir}/**/test-results/*.xml", allowEmptyResults: true, keepLongStdio: true
    }
    if (config.archiveFiles) {
      archiveStageFiles(config.h2o3dir, FILES_TO_ARCHIVE)
      if (config.archiveAdditionalFiles) {
        echo "###### Archiving additional files: ######"
        echo "${config.archiveAdditionalFiles}"
        archiveStageFiles(config.h2o3dir, config.archiveAdditionalFiles)
      }
    }
  }
}

def execMake(makefilePath, target, String h2o3dir) {
  sh """
    export JAVA_HOME=/usr/lib/jvm/java-8-oracle
    locale

    echo "Activating Python ${env.PYTHON_VERSION}"
    . /envs/h2o_env_python${env.PYTHON_VERSION}/bin/activate

    echo "Activating R ${env.R_VERSION}"
    activate_R_${env.R_VERSION}

    cd ${h2o3dir}
    echo "Linking small and bigdata"
    rm -f smalldata
    ln -s -f /home/0xdiag/smalldata
    rm -f bigdata
    ln -s -f /home/0xdiag/bigdata

    # The Gradle fails if there is a special character, in these variables
    unset CHANGE_AUTHOR_DISPLAY_NAME
    unset CHANGE_TITLE

    echo "Running Make"
    make -f ${makefilePath} ${target}
  """
}

def archiveStageFiles(h2o3dir, files) {
  archiveArtifacts artifacts: files.collect{"${h2o3dir}/${it}"}.join(', '), allowEmptyArchive: true
}

return this
