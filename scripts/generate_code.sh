#!/bin/bash

#
# Print error message and exit
#
die() {
    echo $1
    exit 1
}

# Check command-line arguments
[ $# == 4 ] || die "usage: $0 <clean_lines> <f or b> <json_dir> <output_dir>"
CLEAN_LINES=$1
SUFFIX=$2
JSON_DIR=$3
OUTPUT_DIR=$4

mkdir -p $OUTPUT_DIR

while read p; do
    FILENAME=$(echo $p | cut -d' ' -f1)
    ID=$(echo $FILENAME | cut -d'.' -f1)

    SIGNATURE=$(echo $p | cut -d' ' -f2)    
    SIGNATURE_FIXED=$(echo ${SIGNATURE/%?/})

    JSON_KEY=$(cat $JSON_DIR/$ID$SUFFIX.json | jq .keyMap.\"$SIGNATURE_FIXED\")

    if [ "$JSON_KEY" == "null" ]
    then
        echo "skipped a line "$SIGNATURE_FIXED
    else
        SOURCE_CODE=$(cat $JSON_DIR/$ID$SUFFIX.json | jq -r .$JSON_KEY)
        echo "$SOURCE_CODE" > $OUTPUT_DIR/${SIGNATURE_FIXED//\//-}.block
    fi
done < $CLEAN_LINES