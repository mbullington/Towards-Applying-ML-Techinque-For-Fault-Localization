#!/usr/bin/env bash
#
################################################################################
# This script determines all fixed source code lines in a fixed Defects4J project
# version. It writes the result to a file in the provided output directory; the
# file name is: <project_id>-<bug_id>.fixed.lines
# 
# 
# Requirements:
# - Bash 4+ needs to be installed
# - diff needs to be installed
# - the environment variable D4J_HOME needs to be set and must point to the
#   Defects4J installation that contains all minimized patches.
#
################################################################################

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

# Check command-line arguments
[ $# -eq 3 ] || die "usage: $0 <project_id> <bug_id> <out_dir>"
PID=$1
BID=$2
OUT_DIR=$3

mkdir -p $OUT_DIR
OUT_FILE="$OUT_DIR/$PID-$BID.fixed.lines"

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

# Put the defects4j command on the PATH
PATH=$PATH:$D4J_HOME/framework/bin

# Temporary directory, used to checkout the buggy and fixed version
TMP="/tmp/get_fixed_lines_$$"
mkdir -p $TMP
# Temporary file, used to collect information about all removed and added lines
TMP_LINES="$TMP/all_fixed_lines"


#
# Determine all fixed lines, using the diff between the buggy and fixed version
#
# Checkout the fixed project version
work_dir="$TMP/$PID-$BID"
defects4j checkout -p$PID -v${BID}f -w$work_dir
# Determine and iterate over all modified classes (i.e., patched files)
src_dir=$(grep "d4j.dir.src.classes=" $work_dir/defects4j.build.properties | cut -f2 -d'=')
mod_classes=$(cat $D4J_HOME/framework/projects/$PID/modified_classes/$BID.src)
for class in $mod_classes; do
    file="$(echo $class | tr '.' '/').java";

    # Checkout the fixed project version
    defects4j checkout -p$PID -v${BID}f -w$work_dir
    cp $work_dir/$src_dir/$file "$TMP/fixed"

    # Checkout the buggy project version
    defects4j checkout -p$PID -v${BID}b -w$work_dir
    cp $work_dir/$src_dir/$file "$TMP/buggy"

    # Diff between fixed and buggy
    diff \
        --unchanged-line-format='' \
        --old-line-format="$file#%dn#%l%c'\12'" \
        --new-group-format="$file#%df#FIX_OF_OMISSION%c'\12'" \
        "$TMP/fixed" "$TMP/buggy" >> "$TMP_LINES"
done
# Print all removed lines to output file
grep --text -v "FIX_OF_OMISSION" "$TMP_LINES" > "$OUT_FILE"

# Check which added lines need to be added to the output file
for entry in $(grep --text 'FIX_OF_OMISSION' "$TMP_LINES"); do
    # Determine whether file#line already exists in output file -> if so, skip
    line=$(echo $entry)
    grep -q "$entry" "$OUT_FILE" || echo "$entry" >> "$OUT_FILE"
done
