battlecode2014
==============

INSTALLATION
============

Install Battlecode package (e.g. on OSX it installs to /Applications/Battlecode2014 by default)

Clone Github repo into your Battlecode install dir e.g. on OSX:

```
cd /Applications/Battlecode2014
git init
rm .gitignore
```

Then add this to your /Applications/Battlecode2014/.git/config file

```
[remote "origin"]
    url = git@github.com:optimuscoprime/battlecode2014.git
    fetch = +refs/heads/*:refs/remotes/origin/*
[branch "master"]
    remote = origin
    merge = refs/heads/master
```

The pull the code from git

```
git pull
```

Then check that add, commit and push work too e.g.

```
nano README.md
# make some trivial change
git add .
git commit -a
git push
git pull
```

The maps directory and teams directory are tracked in Git. So is the testbed directory.

PS. This would be much simpler if you could just clone into the /Applications/Battlecode2014 directory, but Git will not allow a clone into a non-empty directory.
.
.
.
.
.
.
.
.
.

TODO DOUBLE CHECK OLD STUFF
=========

Lecture Notes
=============

http://optimuscopri.me/wiki/Battlecode2014


Steps To Setup
==============

Assumes Java, Ant and github are already setup.

## Install Battlecode App

Download installer from battlecode

Install

$ java -jar battlecode-x.x.x.jar

Follow prompts.

On Mac installs to /Applications/Battlecode2014

## Clone repo

```
$ mkdir -p ~/github/optimuscoprime
$ git clone git@github.com:optimuscoprime/battlecode2014.git
```

## Link repo to App Dir

Symlinked maps and players into the app
(Steve did this last year, but Rupert and Ian did something more like setup
the Battlecode app in the repo, or symlinked the whole app dir to the repo).

```
$ cd /Applications/Battlecode2014/maps
$ ln -s ~/github/optimuscoprime/battlecode2014/maps/simple.xml
$ cd /Applications/Battlecode2014/teams
$ ln -s ~/github/optimuscoprime/battlecode2014/sc01
$ ln -s ~/github/optimuscoprime/battlecode2014/sc02
```

Probably should either write a script, or work out a better way to link
the repo to the app dir.

## Run app

```
$ cd /Applications/Battlecode2014
$ ant run
```

## Upload Player

In repo:

```
$ cd team085
$ ruby make.rb sc01
```

Here change 'sc01' to the name (ie folder and java package name) of the bot you want to package.

Should set things up right the first time and all subsequent times build a submission.jar at

/Applications/Battlecode2014/submission.jar

Log in, Upload Player, submit that bot.

(I would automate the upload, but people did that last year and got penalised or capped, or something)

Tries to limit assumptions about file locations, but check the script first if you're worried about it not working.
If you find a part that doesn't work for your setup you can wrap that line in ruby like this:

```
if `hostname`.strip == "my-laptop-hostname"
   # run my version of this code
else
   # run the existing version of the code
end
```



