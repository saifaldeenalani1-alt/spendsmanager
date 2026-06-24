@if "%DEBUG%"=="" @echo off
@rem Use the local gradle wrapper
set DIRNAME=%~dp0
"%JAVA_HOME%/bin/java" -classpath "%DIRNAME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
