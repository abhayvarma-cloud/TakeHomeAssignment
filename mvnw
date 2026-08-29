#!/bin/sh
# Maven wrapper script for Unix

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(pwd)}"

if [ -z "$JAVA_HOME" ] ; then
  JAVACMD=java
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

MAVEN_HOME="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper"
MAVEN_WRAPPER_JAR="${MAVEN_HOME}/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES="${MAVEN_HOME}/maven-wrapper.properties"

if [ -r "$MAVEN_WRAPPER_PROPERTIES" ] ; then
  DOWNLOAD_URL=$(grep distributionUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2-)
fi

# Download wrapper jar if not present
if [ ! -r "$MAVEN_WRAPPER_JAR" ]; then
  echo "Downloading maven-wrapper.jar ..."
  if command -v curl > /dev/null; then
    curl -o "${MAVEN_WRAPPER_JAR}" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  elif command -v wget > /dev/null; then
    wget -O "${MAVEN_WRAPPER_JAR}" "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  fi
fi

exec "$JAVACMD" \
  -classpath "$MAVEN_WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
