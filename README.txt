########################
#EU4 save game replayer#
########################

This is a fan WiP project coded in free time and originally intended for my personal use only as JavaFX exercise, so there's no warranty, use only on your own risk! You will probably need at least jre7. Some forumites reported problems with using 32b Java on 64b computer that were fixed by installing 64b Java.
Before starting, set "eu4.dir" property in file "replayer.properties" to directory containing the game. Property "days.per.tick" controls the speed of the replay.
There's a bug in javafx which sometimes prevents the map from refreshing. If this is your case and Settings->Emergency Refresh does not help you, download jre7 update 40 or newer; or backup provinces.bmp and experiment with its size (for me the width has to be 4096 pixels or less, it likely depends on your graphic card).
If you mess with (un)checking controller or owner events during replaying, you may get weird results.

Naturally the more event types you want to track, the slower the replaying is.

Tested on only one machine and one complete vanilla save started in 1444.11.11, so it may not work properly with yours.
Submit any bugs, problems and feature requests on Paradox forums or PM me (nick PreXident), maybe we can figure it out.
http://forum.paradoxplaza.com/forum/showthread.php?722493-UTILITY-Java-Save-Game-Replayer

If you want to replay ironman save, try this approach: backup, load, load other, load, save locally, restore your backup

############
#Used files#
############

map/default.map
map/provinces.bmp
map/definition.csv
common/country_tags/* + files mentioned in there (typically common/countries/*)
common/religions/*
common/cultures/*

############
#Known Bugs#
############

In province history there's sometimes missing record of regaining province control from rebels. So in the end there are more rebel controlled provinces than there should be. If you have any idea why this is happening or how to detect this, please share!

##################
#Planned features#
##################

- selectable provinces
- fast forward replay to any given date, not only end date
- nation focus to display only selected state and its PU
- monarchs, wars, ...
- back rewind

############
#Change log#
############

0.9
Added optional borders - set borders=true in properties
CSS file is now used for log styling to get rid of the unpleasant scrollbar behaviour

0.8
Added province culture map mode - cultures without primary country tag get generated color

0.7
F5 for refresh
Controls rearranged
Buttons +/- removed
Resizable log
Zooming with mousewheel, centring map if too small and better zooming in general

0.6
Added province religion map mode

0.5
Added property gif.new.file which tells after how many frames new gif file should be created, 0 means never

0.4
Non 1444.11.11 starts should work now

0.3
Displaying info when hovering over provinces

0.2
Setting the speed of replay in more user-friendly way
Experimental gif export - set gif=true in properties and use fastforwarding, gif will be created in dir with savegame, its size can be influenced by properties gif.width and gif.height

0.1
Initial release

#########
#Sources#
#########

If anyone is interested or paranoid enough, sources are freely available, just PM me. However they are no masterpiece I am afraid ;-) It's a netbeans free form project, so you just need to adjust properties in build.xml if JAVA_HOME is not set and then use ant.

################
#Used Libraries#
################
Elliot Kroo's GifSequenceWriter under Creative Commons Attribution 3.0 Unported License.
PositionInputStream by erickson.