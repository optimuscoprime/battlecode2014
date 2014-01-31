#!/bin/bash

ls ../maps | sed -r 's/^/\t\tmaps.add("/g' | sed -r 's/$/");/g'
