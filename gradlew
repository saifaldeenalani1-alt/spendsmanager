#!/bin/sh
app_path=$(cd "$(dirname "$0")"; pwd)
export APP_HOME="$app_path"
exec "$JAVA_HOME/bin/java" \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
