def call(final buildConfig) {
  return [
    "JAVA_VERSION=8",
    "BUILD_HADOOP=${buildConfig.getBuildHadoop()}"
  ]
}

return this
