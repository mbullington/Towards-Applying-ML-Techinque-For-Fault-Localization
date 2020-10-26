#!/bin/bash

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

# Check command-line arguments
[ $# == 3 ] || die "usage: $0 <temp_dir> <output_buggy> <output_fixed>"
TEMP_DIR=$1
OUTPUT_FILE_BUGGY=$2
OUTPUT_FILE_FIXED=$3

rm $OUTPUT_FILE_BUGGY
touch $OUTPUT_FILE_BUGGY

for fi in $TEMP_DIR/*.buggy.lines ; do
    BASE_FILENAME=$(basename $fi)
    while read p; do
        if ! grep -q FAULT_OF_OMISSION <<< $p
        then
            echo "$BASE_FILENAME    $p" >> $OUTPUT_FILE_BUGGY
        fi
    done < $fi
done

rm $OUTPUT_FILE_FIXED
touch $OUTPUT_FILE_FIXED

for fi in $TEMP_DIR/*.fixed.lines ; do
    BASE_FILENAME=$(basename $fi)
    while read p; do
        if ! grep -q FIX_OF_OMISSION <<< $p
        then
            echo "$BASE_FILENAME    $p" >> $OUTPUT_FILE_FIXED
        fi
    done < $fi
done