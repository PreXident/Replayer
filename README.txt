########################
#EU4 save game replayer#
########################

This is a fan WiP project coded in free time and originally intended for my personal use only as JavaFX exercise, so there's no warranty, use only on your own risk! You will probably need at least jre7. Some forumites reported problems with using 32b Java on 64b computer that were fixed by installing 64b Java.

There's a bug in javafx which sometimes prevents the map from refreshing. If this is your case and Settings->Emergency Refresh does not help you, download jre7 update 40 or newer; or backup provinces.bmp and experiment with its size (for me the width has to be 4096 pixels or less, it likely depends on your graphic card).
If you mess with (un)checking notable events during replaying, you may get weird results.

Naturally the more event types you want to track, the slower the replaying is.

Tested on only one machine and one complete vanilla save started in 1444.11.11, so it may not work properly with yours.
Submit any bugs, problems and feature requests on Paradox forums or PM me (nick PreXident), maybe we can figure it out.
http://forum.paradoxplaza.com/forum/showthread.php?722493-UTILITY-Java-Save-Game-Replayer

If you want to replay ironman save, try this approach: backup, load, load other, load, save locally, restore your backup (tested pre 1.2)

############
#Used files#
############

map/default.map
map/provinces.bmp
map/definition.csv
common/country_tags/* + files mentioned in there (typically common/countries/*)
common/religions/*
common/cultures/*
common/colonial_regions/
common/defines.lua

############
#Known Bugs#
############

In province history there's sometimes missing record of regaining province control from rebels. So in the end there are more rebel controlled provinces than there should be. If you have any idea why this is happening or how to detect this, please share!
Feature subjects.as.overlord is not perfect, as it can display only relations that exist at save game end because others are not stored in saves. It means that a vassal who broke free later on is not displayed as part of his temporary overlord. The issue could be less serious if you replay batch of saves instead of only one file.

##################
#Planned features#
##################

- randomized new world
- monarchs, wars, ...
- nation focus to display PUs (properly, not only from active_relations)
- display battles 
- binary saves

############
#Change log#
############

1.5
Batch Saves - you can now select multiple saves to be replayed, they will be sorted alphabetically and information from every save above the first one will be added to the first one successively (this is necessary for technology mapmode and others that use information stored in save outside history)
Technology mapmode in two versions (for both brighter is better):
- combined where all tech branches are combined to green color
- separate where red part of country color is military, blue diplomatic and green administrative technology level

1.4b
Added mouse coordinates to province hover hint (thx to sinkingmist for suggestion)

1.4
Colonial nations existing on game start should be displayed correctly
Encoding of input files changed to LATIN-1, so Osel etc should be displayed correctly
Province information is reset on loading a save
Added feature to display subjects (colonials, protectorates, vassals, PUs) as part of their overlords; set property subjects.as.overlord=true (thx to Toa Kraka for suggestion)
Fixed appearence for Java8 release candidate
Added localization support (for now en and cs)
Empty lines in file map/definition.csv are now skipped

1.3
Fixed replaced_path, so games converted from CK2 actually work

1.2
Updated to CoP
Unfortunately no support for Randomized New World and problems with colonial nations (displayed on game start (ie non 1444 starts) as part of motherland)
Add a bunch of new gif related properties (thx to ferluciCZ for suggesting this feature):
gif.subimage as a flag that only section of the map should be exported to gif; other properties specify the exported part
gif.subimage.x
gif.subimage.y
gif.subimage.width
gif.subimage.height
You will probably want to change gif.width and gif.height so the gif is not stretched

1.1
Added a bunch of new gif related properties:
#flag indicating whether the date should be drawn to gif
gif.date.draw=true
#font color of the gif date; use hexadecimal or octal number
gif.date.color=0x000000
#font size of the gif date
gif.date.size=12
#x-coord of the gif date
gif.date.x=60
#y-coord of the gif date
gif.date.y=60
Also pressing >> when at end date should not cause progressbar to run from one side to the other.

1.0
Fixed an (probably mostly silent) exception thrown when empty or no mod is specified

0.13
Jumping by '->' can now be cancelled by '||'
Added experimental mod support - property mod.basedir should point to the game's directory in your documents (e.g. C\:\\Users\\System_Lord\\Documents\\Paradox Interactive\\Europa Universalis IV) if it is not in the default place; and property mod.list should contain semicolon separated list of mods to load (e.g. mod/iu.mod;mod/test.mod). The order in the list is important!

0.12
If eu4.dir is invalid when program loads, user is prompted to choose a valid one.
When save directory from properties is invalid, program tries to locate it at default place (OS dependant). If even that fails, user's home dir is selected.
Added fast rewind (<<) that as well as fast forward (>>, formerly >|) can be now stopped by pause (||).
Extended days.per.tick into delta.per.tick and period.per.tick - so users can easily specify eg. 2 Months per tick.

0.11
You can now jump to any date within save game, but log content is replaced, not appended
Added selectable provinces, info and events related to the selected province are displayed in a separated log

0.10
Added focus feature in settings to display only controller and owner changes related to a certain tag (set before loading a save!)
Added context menu for clearing the log
Added back rewind

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

If anyone is interested or paranoid enough, sources are freely available. However they are no masterpiece I am afraid ;-) It's a netbeans free form project, so you just need to adjust properties in build.xml if JAVA_HOME is not set and then use ant. Gradle build script is available too (Unfortunately task dist is not yet perfect).
Check the repository PreXident/Replayer at Bitbucket.

##############
#Contributors#
##############

PreXident
Lateralus (Hottemax at Bitbucket)

################
#Used Libraries#
################
Elliot Kroo's GifSequenceWriter under Creative Commons Attribution 3.0 Unported License.
PositionInputStream by erickson.
Apache Commons Compress under Apache License