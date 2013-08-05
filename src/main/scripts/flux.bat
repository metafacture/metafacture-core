@echo off

set DEFAULT_JAVA_OPTS=-Xmx512M

if %JAVA_OPTS% == "" (
	set JAVA_OPTS="%DEFAULT_JAVA_OPTS%"
)

java %JAVA_OPTS% -jar "%~dp0${project.build.finalName}.jar" %*
