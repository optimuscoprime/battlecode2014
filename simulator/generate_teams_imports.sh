#!/bin/bash

ls ../teams | sed -r 's/^/\t\tteams.add("/g' | sed -r 's/$/");/g'
