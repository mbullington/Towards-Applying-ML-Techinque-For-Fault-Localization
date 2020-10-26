#!/usr/bin/env bash

PWD=`pwd`

#
# Print error message and exit
#
die() {
  echo $1
  exit 1
}

# Check command-line arguments
[ $# == 2 ] || die "usage: $0 <input_csv> <output_dir>"
INPUT_CSV=$1
OUTPUT_DIR=$2

mkdir -p $OUTPUT_DIR

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"

## compile utility
mvn clean package || die "Compilation of the java-parser failed!"

while read p; do
  PID=$(echo $p | cut -d',' -f1)
  BID=$(echo $p | cut -d',' -f2)

  "$PWD/_run_java-parser.sh" $PID $BID $OUTPUT_DIR
done < $INPUT_CSV

# Compress data
#tar -cvzf "$PWD/source-code-lines.tar.gz" "source-code-lines"
# Put data in place
#mv -f "$PWD/source-code-lines.tar.gz" "$PWD/../pipeline-scripts/"

echo "DONE!"

# EOF

