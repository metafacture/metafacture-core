#!/bin/bash

METAFACTURE_HOME=$( dirname "$( realpath "$0" )" )

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

java_opts_file="$METAFACTURE_HOME/java-options.conf"
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
