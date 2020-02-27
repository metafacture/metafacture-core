@ECHO OFF
REM
REM Copyright 2017, 2018 Christoph Böhme
REM
REM Licensed under the Apache License, Version 2.0 the "License";
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM     http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM

SETLOCAL EnableDelayedExpansion

SET METAFACTURE_HOME=%~dp0

REM Use the java command available on the path by default.
REM Define FLUX_JAVA_BIN in your environment to use a
REM different executable.
IF "x%FLUX_JAVA_BIN%" == "x" (
    SET FLUX_JAVA_BIN=java
)

SET JAVA_OPTS_FILE="%METAFACTURE_HOME%\config\java-options.conf"
SET JAR_FILE="%METAFACTURE_HOME%@jarfile@"

REM Read JVM options from configuration file. Lines starting
REM with # are treated as comments. Empty lines are ignored.
REM
REM The space character at the end of the following
REM line is important and must not be removed:
SET JAVA_OPTS=
<%JAVA_OPTS_FILE% (
    FOR /F %%I IN ('FINDSTR /N "^" %JAVA_OPTS_FILE%') DO (
        SET  /P LINE=
        IF NOT "x!LINE!" == "x" (
            SET FIRST=!LINE:~0,1!
            IF NOT "!FIRST!" == "#" (
                SET JAVA_OPTS=!JAVA_OPTS! !LINE!
            )
        )
        SET LINE=
    )
)

REM Substitute environment variables in the configuration.
REM Undefined variables remain in the configuration. Since
REM FLUX_JAVA_OPTIONS is included in the configuration by
REM default we make sure that it can always be substituted.
IF "x%FLUX_JAVA_OPTIONS%" == "x" (
    REM The space character at the end of the following
    REM line is important and must not be removed:
    SET FLUX_JAVA_OPTIONS= 
)
FOR /F "tokens=1,* delims==" %%I IN ('SET') DO (
    SET JAVA_OPTS=!JAVA_OPTS:$%%I=%%J!
)

REM Start flux:
%FLUX_JAVA_BIN% !JAVA_OPTS! -jar %JAR_FILE% %*

ENDLOCAL
