#!/bin/bash

TMP_FILE_LOCATION=tmp.tmp.tmp

# would like to do this:
#
# rm *.class
# javac GameRunner.java

javac Overlord.java

java Overlord | tee $TMP_FILE_LOCATION

cat $TMP_FILE_LOCATION | ruby tally_overlord_output.rb

rm -f $TMP_FILE_LOCATION
