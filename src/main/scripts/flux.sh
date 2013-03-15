#!/bin/bash

dir=`dirname $0`

jarfile="${project.build.finalName}.jar"

if uname | grep -iq cygwin; then
    java -jar "`cygpath -am $dir/$jarfile`" $*
else
    java -jar "$dir/$jarfile" $*
fi


