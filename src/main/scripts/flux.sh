#!/bin/bash

dir=`dirname $0`

jarfile="${project.build.finalName}.jar"

if uname | grep -iq cygwin; then
    java -Xmx512M -jar "`cygpath -am $dir/$jarfile`" $*
else
    java -Xmx512M -jar "$dir/$jarfile" $*
fi


