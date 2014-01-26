#!/bin/bash

TMP_FILE_LOCATION=tmp.tmp.tmp

java Overlord | tee $TMP_FILE_LOCATION

cat $TMP_FILE_LOCATION | ruby tally_overlord_output.rb

rm -f $TMP_FILE_LOCATION
