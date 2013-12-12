#!/bin/bash

# `cd` is used with a relative path in this script. We may 
# end in an unexpected location if CDPATH is not empty.
unset CDPATH

if [ -z "$BASH_SOURCE" ] ; then
	echo "Error: cannot determine script location (\$BASH_SOURCE is not set)" >&2
	exit 2
fi
# If a symbolic link is used to invoke this script this link 
# needs to be resolved in order to find the directory where 
# the actual script file lives. Several tools exist for
# resolving symbolic links. However, none of these tools is
# available on all platforms. Hence, we try different ones
# until we succeed.
if [ -L "$BASH_SOURCE" ] ; then
	script_file=$( realpath "$BASH_SOURCE" 2> /dev/null ) ||
	script_file=$( readlink -f "$BASH_SOURCE" 2> /dev/null ) ||
	script_file=$(
		file=$BASH_SOURCE
		while [ -L "$file" ] ; do
			cd "$( dirname "$file" )"
			link_name=$( basename "$file" )
			file=$( readlink "$link_name" 2> /dev/null ) ||
			file=$( ls -ld "$link_name" | sed "s/^.\+ -> \(.\+\)$/\1/g" )
		done
		cd "$( dirname "$file" )"
		echo "$( pwd )/$( basename "$file" )"
	)
else
	script_file=$BASH_SOURCE
fi
# Remove script file name from the path and make sure 
# that the path is absolute:
METAFACTURE_HOME=$( cd "$( dirname "$script_file" )" ; pwd )

# Fix path if running under cygwin:
if uname | grep -iq cygwin ; then
	METAFACTURE_HOME=$( cygpath -am "$METAFACTURE_HOME" )
fi

# Use the java command available on the path by default.
# Define FLUX_JAVA_BIN in your environment to use a 
# different executable.
if [ -z "$FLUX_JAVA_BIN" ] ; then
	FLUX_JAVA_BIN=java
fi

java_opts_file="$METAFACTURE_HOME/config/java-options.conf"
jar_file="$METAFACTURE_HOME/${project.build.finalName}.jar"

# Load java options from configuration file. Lines starting 
# with # are treated as comments. Empty lines are ignored.
java_opts=$( cat "$java_opts_file" | grep -v "^#" | tr "\n\r" "  " )

# Substitute environment variables. Undefined variables 
# remain in the configuration. Since FLUX_JAVA_OPTIONS is 
# included in the configuration by default we make sure that 
# it can always be substituted.
if [ -z "$FLUX_JAVA_OPTIONS" ] ; then
	FLUX_JAVA_OPTIONS=
fi
# This sed script turns the output of set into a sed script
# which replaces the dollar prefixed name of each environment
# variable with its value:
vars_to_script=$( cat <<'EOF'
	s/\\/\\\\/g ;
	s/!/\\!/g ; 
	s/='(.*)'$/=\1/ ;
	s/^([^=]+)=(.*)$/s!\\$\1!\2!g ; /g
EOF
)
substitute_vars_script=$( set | sed -r "$vars_to_script" )
# Substitute environment variables in the java options:
java_opts=$( echo "$java_opts" | sed "$substitute_vars_script")

# Turn java options string into an array to allow passing
# the options as command parameters. Options may be partially
# quoted with single or double quotes and may contain 
# escape sequences. Quotes are removed after splitting because
# the shell quotes the parameters again:
option_pattern="[^\"' ]*(\"[^\"\\]*(\\\\.[^\"\\]*)*\"|'[^'\\]*(\\\\.[^'\\]*)*'|\\.[^\"' ]*)*"
remove_quotes="s/(^[\"'])|(([^\\])[\"'])/\3/g"
java_opts_array=()
while read line ; do
	line=$( echo "$line" | sed -r "$remove_quotes" )
	java_opts_array+=("$line")
done < <( echo "$java_opts" | grep -Eo "$option_pattern" )

# Start flux:
"$FLUX_JAVA_BIN" "${java_opts_array[@]}" -jar "$jar_file" "$@"
