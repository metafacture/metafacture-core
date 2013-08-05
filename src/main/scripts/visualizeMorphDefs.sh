#!/bin/bash

DEFAULT_JAVA_OPTS=-Xmx512M

if [ $# -lt 2 ]
then
  echo "Usage: `basename $0` OUTPUT_FORMAT DOT_FILES+"
  exit 65
fi

if [ "$JAVA_OPTS" == "" ] ; then
	JAVA_OPTS=$DEFAULT_JAVA_OPTS
fi

FORMAT=$1
shift
for FILE in $@
do
    echo visualizing $FILE
    java $JAVA_OPTS org.culturegraph.mf.MorphVis "$FILE" | dot -T$FORMAT > "$FILE.$FORMAT"
done
