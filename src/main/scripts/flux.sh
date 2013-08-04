#!/bin/bash

DEFAULT_JAVA_OPTS=-Xmx512M

DIR=`dirname $0`

JARFILE="${project.build.finalName}.jar"

if $JAVA_OPTS == "" ; then
	JAVA_OPTS = $DEFAULT_JAVA_OPTS
fi

if uname | grep -iq cygwin; then
    java $JAVA_OPTS -jar "`cygpath -am $DIR/$JARFILE`" $*
else
    java $JAVA_OPTS -jar "$DIR/$JARFILE" $*
fi


