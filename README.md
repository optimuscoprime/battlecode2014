battlecode2014
==============

Lecture Notes
=============

http://optimuscopri.me/wiki/Battlecode2014


Steps To Setup
==============

Assumes Java and github are already setup.

## Install Battlecode App

Download installer from battlecode

Install

$ java -jar battlecode-x.x.x.jar

Follow prompts.

On Mac installs to /Applications/Battlecode2014

## Clone repo

$ mkdir -p ~/github/optimuscoprime

$ git clone git@github.com:optimuscoprime/battlecode2014.git

## Link repo to App Dir

Symlinked maps and players into the app
(Steve did this last year, but Rupert and Ian did something more like setup
the Battlecode app in the repo, or symlinked the whole app dir to the repo).

$ cd /Applications/Battlecode2014/maps

$ ln -s ~/github/optimuscoprime/battlecode2014/maps/simple.xml

$ cd /Applications/Battlecode2014/teams

$ ln -s ~/github/optimuscoprime/battlecode2014/sc01

$ ln -s ~/github/optimuscoprime/battlecode2014/sc02

Probably should either write a script, or work out a better way to link
the repo to the app dir.
