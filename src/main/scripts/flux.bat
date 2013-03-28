@echo off
java -Xmx512M -jar "%~dp0${project.build.finalName}.jar" %*