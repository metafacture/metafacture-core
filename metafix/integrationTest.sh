#! /bin/bash

set -e

cd "$(dirname "$(readlink -f "$0")")"

metafix_file=test.flux
catmandu_file=test.cmd

fix_file=test.fix
todo_file=todo.txt

input_glob=input.*
expected_glob=expected.*
expected_errors_extension=err

metafix_output_glob=output-metafix.*
catmandu_output_glob=output-catmandu.*

root_directory="$PWD"
data_directory="$root_directory/src/test/resources/org/metafacture/metafix/integration"
gradle_command="$root_directory/../gradlew"

function parse_boolean() {
  [ "${1,,}" == true ]
}

parse_boolean "$METAFIX_DISABLE_TO_DO" && disable_todo=1 || disable_todo=
parse_boolean "$METAFIX_INTEGRATION_TEST_PROFILE" && noprofile= || noprofile=no

[ -t 1 -a -x /usr/bin/colordiff ] && colordiff=colordiff || colordiff=cat

function _tput() {
  tput -T "${TERM:-dumb}" "$@" || true
}

color_error=$(_tput setaf 1)                # red
color_failed=$(_tput setaf 1)               # red
color_failure=$(_tput bold)$(_tput setaf 1) # bold red
color_info=$(_tput setaf 5)                 # purple
color_invalid=$(_tput setaf 6)              # cyan
color_passed=$(_tput setaf 2)               # green
color_reset=$(_tput sgr0)                   # reset
color_skipped=$(_tput setaf 3)              # yellow
color_success=$(_tput bold)$(_tput setaf 2) # bold green
color_test=$(_tput bold)                    # bold

declare -A tests

failed=0
invalid=0
passed=0
skipped=0

current_file=

function log() {
  echo "$@"
}

function warn() {
  echo "$@" >&2
}

function die() {
  [ $# -gt 0 ] && warn "$@"
  exit 2
}

function run_metafix() {
  $gradle_command -p "$root_directory" :metafix-runner:run --args="$1" -P${noprofile}profile="${1%.*}"
}

function run_catmandu() {
  :
}

nanosecond_length=9
nanosecond_offset=-$nanosecond_length

function current_time() {
  date +%s%N
}

function elapsed_time() {
  local elapsed_time=$(($(current_time) - $1)) seconds=0 milliseconds

  if [ ${#elapsed_time} -gt $nanosecond_length ]; then
    seconds=${elapsed_time:0:$nanosecond_offset}
  else
    elapsed_time=$(printf "%0${nanosecond_length}d" "$elapsed_time")
  fi

  [ "$seconds" -lt 60 ] && milliseconds=".${elapsed_time:$nanosecond_offset:3}"

  echo " ($(date "+%-Hh %-Mm %-S${milliseconds}s" -ud "@$seconds" | sed 's/^\(0[hm] \)*//'))"
}

function get_file() {
  local test=$1 type=$2 reason; shift 2

  if [ $# -ne 1 ]; then
    reason="Ambiguous $type files: $*"
  elif [ ! -r "$1" ]; then
    reason="No $type file: $1"
  elif [ "$type" != "output" ] && [ ! -s "$1" ]; then
    reason="Empty $type file: $1"
  else
    current_file=$1
    return 0
  fi

  log "$color_test$test$color_reset: ${color_invalid}INVALID$color_reset ($reason)"

  ((invalid++)) || true

  return 1
}

function command_info() {
  log "  ${color_info}${1^} command exit status$color_reset: $2"

  [ -s "$3" ] && log "  ${color_info}${1^} command output$color_reset: $3" || rm -f "$3"
  [ -s "$4" ] && log "  ${color_info}${1^} command error$color_reset:  $4" || rm -f "$4"

  log
}

function skip_test() {
  if [ -r "$2" ]; then
    local message="$color_test$1$color_reset: ${color_skipped}SKIPPED$color_reset" reason=$(head -1 "$2")

    [ -n "$reason" ] && message+=" ($reason)"
    log "$message"

    ((skipped++)) || true

    return 0;
  else
    return 1;
  fi
}

function test_passed() {
  if [ -r "$2" ]; then
    log "$color_test$1$color_reset: ${color_failed}FAILED$color_reset (Marked as \"to do\", but passed.)"

    ((failed++)) || true
  else
    if parse_boolean "$METAFIX_LOG_PASSED"; then
      log "$color_test$1$color_reset: ${color_passed}PASSED$color_reset$3"
    fi

    ((passed++)) || true
  fi
}

function test_failed() {
  if ! skip_test "$1" "$2"; then
    log "$color_test$1$color_reset: $color_failed$4$color_reset$3"

    if [ $# -ge 13 ]; then
      log "  Fix:      $9"
      log "  Input:    ${10}"
      log "  Expected: ${11}"
      log "  Output:   ${12}"
      log "  Diff:     ${13}"

      [ -s "${13}" ] && $colordiff <"${13}" || rm -f "${13}"
    fi

    command_info "$5" "$6" "$7" "$8"

    ((failed++)) || true
  fi
}

function run_tests() {
  local test matched=1\
    test_directory test_fix test_input test_expected test_todo\
    metafix_command_output metafix_command_error metafix_start_time\
    metafix_exit_status metafix_output metafix_diff metafix_elapsed_time

  cd "$data_directory"

  for test in $(find */ -type f -path "$1/$metafix_file" -printf "%h\n"); do
    matched=0

    [ -n "${tests[$test]}" ] && continue || tests[$test]=1

    test_directory="$PWD/$test"

    get_file "$test" Fix "$test_directory"/$fix_file || { log; continue; }
    test_fix=$current_file

    get_file "$test" input "$test_directory"/$input_glob || { log; continue; }
    test_input=$current_file

    get_file "$test" expected "$test_directory"/$expected_glob || { log; continue; }
    test_expected=$current_file

    test_todo="$test_directory/$todo_file"

    if [ -z "$disable_todo" ] || ! skip_test "$test" "$test_todo"; then
      # TODO: catmandu (optional)

      metafix_command_output="$test_directory/metafix.out"
      metafix_command_error="$test_directory/metafix.err"

      metafix_start_time=$(current_time)

      run_metafix "$test_directory/$metafix_file" >"$metafix_command_output" 2>"$metafix_command_error"
      metafix_exit_status=$?

      metafix_elapsed_time=$(elapsed_time "$metafix_start_time")

      if [ "$metafix_exit_status" -eq 0 ]; then
        if get_file "$test" output "$test_directory"/$metafix_output_glob; then
          metafix_output=$current_file
          metafix_diff="$test_directory/metafix.diff"

          if diff -u "$test_expected" "$metafix_output" >"$metafix_diff"; then
            test_passed "$test" "$test_todo" "$metafix_elapsed_time"

            rm -f "$metafix_diff" "$metafix_command_output" "$metafix_command_error"
          else
            test_failed "$test" "$test_todo" "$metafix_elapsed_time" FAILED\
              metafix "$metafix_exit_status" "$metafix_command_output" "$metafix_command_error"\
              "$test_fix" "$test_input" "$test_expected" "$metafix_output" "$metafix_diff"
          fi
        else
          command_info metafix "$metafix_exit_status" "$metafix_command_output" "$metafix_command_error"
        fi
      elif [ "${test_expected##*.}" == "$expected_errors_extension" ]; then
        get_file "$test" error "$metafix_command_error" || { log; continue; }

        while read -r pattern; do
          if ! grep -qE "$pattern" "$metafix_command_error"; then
            test_failed "$test" "$test_todo" " (Pattern not found: $pattern)" FAILED\
              metafix "$metafix_exit_status" "$metafix_command_output" "$metafix_command_error"

            continue 2
          fi
        done <"$test_expected"

        test_passed "$test" "$test_todo" "$metafix_elapsed_time"
      else
        test_failed "$test" "$test_todo" "$metafix_elapsed_time" ERROR\
          metafix "$metafix_exit_status" "$metafix_command_output" "$metafix_command_error"
      fi
    fi
  done

  cd - >/dev/null

  return $matched
}

start_time=$(current_time)

if [ $# -eq 0 ]; then
  run_tests '*' || true
else
  for pattern in "$@"; do
    run_tests "$pattern" || warn "No tests matching pattern: $pattern"
  done
fi

[ ${#tests[@]} -gt 0 ] || die "No tests found: $data_directory"

summary="${color_passed}$passed passed$color_reset"
summary+=", ${color_failed}$failed failed$color_reset"
summary+=", ${color_skipped}$skipped skipped$color_reset"
summary+=", ${color_invalid}$invalid invalid$color_reset"
summary+=$(elapsed_time "$start_time")

if [ $failed -gt 0 -o $invalid -gt 0 ]; then
  log "${color_failure}FAILURE$color_reset: $summary"
  exit 1
else
  log "${color_success}SUCCESS$color_reset: $summary"
  exit 0
fi
