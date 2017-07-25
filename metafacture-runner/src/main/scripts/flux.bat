@ECHO OFF

SETLOCAL EnableDelayedExpansion

SET METAFACTURE_HOME=%~dp0

REM Use the java command available on the path by default.
REM Define FLUX_JAVA_BIN in your environment to use a 
REM different executable.
IF "x%FLUX_JAVA_BIN%" == "x" (
	SET FLUX_JAVA_BIN=java
)

SET JAVA_OPTS_FILE="%METAFACTURE_HOME%\config\java-options.conf"
SET JAR_FILE="%METAFACTURE_HOME%${project.build.finalName}.jar"

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