#!/usr/bin/env bash
# http://stackoverflow.com/questions/11942306/how-to-replace-a-string-in-multiple-files-using-grep-and-sed-when-the-i-argumen

if [ "$#" -ne 3 ] || ! [ -d "$1" ]; then
  echo "Usage: $0 DIRECTORY  string_find string_replace" >&2
  exit 1
fi


#grep '/static' dir/* | xargs sed -i 's/\/static//g'

# on osx:
grep -l "$2" $1/* | xargs sed -i "" "s/$2/$3/g"
