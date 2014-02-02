battlecode2014
==============

FINAL PLAYER
============

The final player we submitted is "marcel" (== "team085")

For a long time before that, our best player was "sound04"

INSTALLATION
============

Idea: clone first, then install official code into the cloned directory.

The maps and teams folder are tracked by git. (And the testbed folder).

Step 1
------

```
git clone git@github.com:optimuscoprime/battlecode2014.git
```

Step 2
------

Then do the Battlecode release install (into the directory that you cloned into)

.

SIMULATOR
=========

To run the simulator, try

Single-threaded mode
--------------------

Pick teams and maps in /simulator/sim.yml

Then run run.rb e.g.

```
ruby run.rb
```

Multi-threaded mode
-------------------

Pick teams and maps in Overlord.java

Then run runall.sh e.g.

```
./runall.sh
```
