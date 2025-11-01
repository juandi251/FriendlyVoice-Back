@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Optional ENV vars
@REM   JAVA_HOME - location of a JDK home dir, required when download maven via java source
@REM   MVNW_REPOURL - repo url base for downloading maven distribution
@REM   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM   MVNW_VERBOSE - true: enable verbose log; others: silence the output
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_WRAPPER_LAUNCHER__=org.apache.maven.wrapper.MavenWrapperMain

@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@REM set %HOME% to equivalent of $HOME
@IF "%HOME%" == "" (SET "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
@IF NOT "%MVNW_SKIP_RC%"=="" @GOTO skipRcPre
@IF EXIST "%USERPROFILE%\mavenrc_pre.bat" @CALL "%USERPROFILE%\mavenrc_pre.bat" %*
@IF EXIST "%USERPROFILE%\mavenrc_pre.cmd" @CALL "%USERPROFILE%\mavenrc_pre.cmd" %*
:skipRcPre

@SETLOCAL

@SET ERROR_CODE=0

@REM ==== START VALIDATION ====
@IF NOT "%JAVA_HOME%"=="" @GOTO OkJHome
@FOR %%i IN (java.exe) DO @SET "JAVACMD=%%~$PATH:i"
@GOTO checkJCmd

:OkJHome
@SET "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJCmd
@IF EXIST "%JAVACMD%" @GOTO chkMHome

@ECHO The JAVA_HOME environment variable is not defined correctly >&2
@ECHO This environment variable is needed to run this program >&2
@ECHO NB: JAVA_HOME should point to a JDK not a JRE >&2
@GOTO error

:chkMHome
@SET "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" @GOTO endDetectBaseDir

@SET "EXEC_DIR=%CD%"
@SET "WDIR=%EXEC_DIR%"

:findBaseDir
@IF EXIST "%WDIR%\.mvn" @GOTO baseDirFound
@CD ..
@SET "WDIR=%CD%"
@IF "%WDIR%"=="%CD%" @GOTO baseDirNotFound
@GOTO findBaseDir

:baseDirFound
@SET "MAVEN_PROJECTBASEDIR=%WDIR%"
@CD "%EXEC_DIR%"
@GOTO endDetectBaseDir

:baseDirNotFound
@SET "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"
@CD "%EXEC_DIR%"

:endDetectBaseDir

@IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config" @GOTO endReadAdditionalConfig

@SETLOCAL EnableExtensions EnableDelayedExpansion
@FOR /F "usebackq delims=" %%a IN ("%MAVEN_PROJECTBASEDIR%\.mvn\jvm.config") DO @SET JVM_CONFIG_MAVEN_PROPS=!JVM_CONFIG_MAVEN_PROPS! %%a
@ENDLOCAL & @SET JVM_CONFIG_MAVEN_PROPS=%JVM_CONFIG_MAVEN_PROPS%

:endReadAdditionalConfig

@SET MAVEN_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    @IF "%%A"=="wrapperUrl" @SET WRAPPER_URL=%%B
)

@REM Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
@REM This allows using the maven wrapper in projects that prohibit checking in binary data.
@IF EXIST %WRAPPER_JAR% (
    @IF "%MVNW_VERBOSE%" == "true" (
        @ECHO Found %WRAPPER_JAR%
    )
) ELSE (
    @IF NOT "%MVNW_REPOURL%" == "" (
        @SET WRAPPER_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
    )
    @IF "%MVNW_VERBOSE%" == "true" (
        @ECHO Couldn't find %WRAPPER_JAR%, downloading it ...
        @ECHO Downloading from: %WRAPPER_URL%
    )

    @POWERSHELL -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"^
        "}"
    @IF "%MVNW_VERBOSE%" == "true" (
        @ECHO Finished downloading %WRAPPER_JAR%
    )
)
@REM End of extension

@REM If specified, validate the SHA-256 sum of the Maven wrapper jar file
@SET WRAPPER_SHA_256_SUM=""
@FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    @IF "%%A"=="wrapperSha256Sum" @SET WRAPPER_SHA_256_SUM=%%B
)
@IF NOT %WRAPPER_SHA_256_SUM%=="" (
    @POWERSHELL -Command "&{"^
       "$hash = (Get-FileHash \"%WRAPPER_JAR%\" -Algorithm SHA256).Hash.ToLower();"^
       "If('%WRAPPER_SHA_256_SUM%' -ne $hash){"^
       "  Write-Output 'Error: Failed to validate Maven wrapper SHA-256, your Maven wrapper might be compromised.';"^
       "  Write-Output 'Investigate or delete %WRAPPER_JAR% to attempt a clean download.';"^
       "  Write-Output 'If you updated your Maven version, you need to update the specified wrapperSha256Sum property.';"^
       "  exit 1;"^
       "}"^
       "}"
    @IF ERRORLEVEL 1 @GOTO error
)

@ENDLOCAL

:runm2e
@SET MAVEN_SKIP_RC=1

@"%MAVEN_JAVA_EXE%" ^
  %JVM_CONFIG_MAVEN_PROPS% ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
@IF ERRORLEVEL 1 @GOTO error
@GOTO end

:error
@SET ERROR_CODE=1

:end
@ENDLOCAL & @SET ERROR_CODE=%ERROR_CODE%

@IF NOT "%MAVEN_SKIP_RC%"=="" @GOTO skipRcPost
@IF EXIST "%USERPROFILE%\mavenrc_post.bat" @CALL "%USERPROFILE%\mavenrc_post.bat"
@IF EXIST "%USERPROFILE%\mavenrc_post.cmd" @CALL "%USERPROFILE%\mavenrc_post.cmd"
:skipRcPost

@IF "%FORCE_EXIT_ON_ERROR%" == "on" (
  @IF %ERROR_CODE% NEQ 0 @EXIT /B %ERROR_CODE%
)

@EXIT /B %ERROR_CODE%
