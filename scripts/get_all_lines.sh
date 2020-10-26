#!/bin/bash

# Calls get_buggy_lines with each CSV entry from <input_csv> with the format ProjectID,BugID
# eg. Chart,1

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

# Check command-line arguments
[ $# == 2 ] || die "usage: $0 <input_csv> <temp_dir>"
INPUT_CSV=$1
TEMP_DIR=$2

while read p; do
  PID=$(echo $p | cut -d',' -f1)
  BID=$(echo $p | cut -d',' -f2)
  ./scripts/get_buggy_lines.sh $PID $BID $TEMP_DIR
  ./scripts/get_fixed_lines.sh $PID $BID $TEMP_DIR
done < $INPUT_CSV