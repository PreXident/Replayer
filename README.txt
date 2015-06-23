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

Video tutorial for version 1.1:
http://www.youtube.com/watch?v=kPdwBscCszY

###############
#Creating gifs#
###############

Since 1.6 there is GUI for creating gifs, however you can still use properties based approach:
As hinted in changelog, set gif=true in replayer.properties and use '>>' to fastforward to the end date. The gif is created in the folder with save game and its size is affected by properties gif.width and gif.height. The sampling period can be set in settings (Per Tick), I recommend 1 Year. If you save frames to gif too often (like every two days), it will take a long time, the file will be huge and few other applications will be able to handle it. To prevent this, you can set property gif.new.file to a positive integral value, and after that number of frames new gif file will be created. Property gif.step specifies number of ms between gif frames. See change log for version 1.1 for additional gif related properties.
As of 1.2 you can also export only part of the map, using new properties (thx to ferluciCZ for suggesting this feature). For Europe it could look like this:
gif=true
gif.width=1100
gif.height=900
gif.subimage=true
gif.subimage.x=2400
gif.subimage.y=0
gif.subimage.width=1100
gif.subimage.height=900

##################
#Random New World#
##################

Random New World feature is supported however it is a bit clumsy and needs user to take part in the process. You also need at least two saves to display the RNW properly - first from the very start of your game and second the actual save to replay. This is needed as the randomizer does not log its actions to province histories, so they do not match the actual game situation. I am not sure if this works with mods, you have to try it.
If you think you can handle it, follow this procedure:
1. Start replayer (if you are using mods, specify them in mod.list property)
2. Select Generator->Generate, which creates new mod RNW in your EU4 mod directory. Close the replayer.
3. Start EU4 with the mods including RNW. It will take a long time as there is a tag for every colonizable province (1400+)
4. Start a new game, select a nation to play (just to be sure from the old world), do NOT check Random New World!!!
5. After the start do not unpause and immediately save the game
6. Edit this new save (it will be circa 300MB, so not every editor can handle it, I use PSPad in hex mode), add random_world=XXX after mod section, replacing XXX for the value in saves you want to replay. Also in setgameplayoptions section change the 7th number to 1
7. Load the modified save, do not unpause, change to political mapmode, hit F10 to save the mapmode screenshot
8. Edit replayer.properties, change rnw.map to path to the mapmode screenshot, eg. c:/Users/System_Lord/Documents/Paradox Interactive/Europa Universalis IV/Screenshots/eu4_map_AHW_1444_11_11_1.png (do not forget to double \ or replace them with /)
9. Start the replayer again and load the saves you want to replay (make sure their alphabetical order matches the logical one)
10. PROFIT!

############
#Used files#
############

map/default.map
map/provinces.bmp
map/definition.csv
map/climate.txt
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
There can be information loss on tag changes, history is transfered to new tag and old tag is left history-less. Consider this scenario:
FRA accumulates some history
FRA changes to RFR - FRA history is transfered to RFR which now contains also 'changed_tag_from="FRA"'
RFR changes back to FRA - RFR history is transfered back to FRA and now contains 'changed_tag_from="RFR"' (this also means that FRA history contains 'changed_tag_from="FRA"' which is kind of funny)
FRA is completely crushed and wiped from the face of Earth
ORL (or other French minor) forms FRA - ORL history now replaces the original FRA history, so information about FRA changing to RFR and back (and also original FRA monarchs and RFR monarchs) is lost forever :-/
In such situations the replayer knows nothing about FRA->RFR->FRA, so the replay cannot be accurate. Multiple saves replay could help to solve this.

##################
#Planned features#
##################

- unrest map mode
- support for dynamic province names
- memory optimalizations
- monarchs, wars, ...
- nation focus to display PUs (properly, not only from active_relations)
- display battles

############
#Change log#
############

1.14
Improved compatibility with EU4 1.12

1.13
Less strict requirements on definition.csv format
Implemented core2owner feature - when cheats are used to change owner of a province, it is not saved in the province history. Use fix.core2owner=true in replayer.properties to force changing owner if core is added. This should make the map more accurate eventually, but of course if the country already has core, it does not help. So if cheats are used to change province owner, consider also removing the new owner's core if it exists.
Improved compatibility with EU4 1.10
Fixed silent NPE when using province borders
System (use "javaw -Dfix.not.years.1.2=true" instead of "javaw" in run.bat) property "fix.not.years.1.2" now controls whether years 1 and 2 will be treated as null
Implemented fix goods2owner for migration and colony destruction. In short, if trade goods is removed (set to unknown) from a  province, the province is removed from its owner. Of course, Trade Goods must be checked in Settings. I think this should work well for vanilla, not sure about mods. Use "fix.goods2owner=false" in replayer.properties to switch it off.
Added feature to de-ironman binary savegame while keeping it binary (also works for multiplayer saves). Such converted file can be loaded into the game and saved again in textual format. This should more stable and reliable than direct converting to text format by the Replayer.

1.12
Improved compatibility with EU4 1.9

1.11
Rewritten conversion of binary saves

1.10
Updated to work with 1.8 saves
Support for compressed saves

1.9
Added option to save replays for later replaying. Useful when you spent a lot of time parsing tens/hundreds of saves. There is no guarantee that old replays will work when newer version of the replayer is released.
Fixed control for Subjects as overlords.
Fixed problems with saves containing manual province name changes.

1.8
Focus can now target multiple tags, just list them separated by non-letter character
Added GUI controls to edit gif.width and gif.height (thx to yahiko for reporting)
If provinces share their color in map/definition.csv, only warning is printed instead of throwing an exception. Only the first province is registered, others are ignored.
Added ModSelector, so you do not need to edit replayer.properties for setting mod.list. Just run mod-selector.bat
French translation - many thanks to Yahiko!

1.7
Info is now printed when mod is about to be loaded
Files common/country_tags/* can now contain # comments on non-separate lines
Added experimental support for binary saves, quite slow though! The testing sample was quite small, so many tokens may be missing, please report any problems ASAP. To just convert saves from binary format to plain text run converter.bat


1.6
Borders and subjects as overlords can be set in settings (restart is needed however)
Path to EU4 directory can be loaded from environment variable EU4_HOME if not specified in the property file
Added GUI for controlling gif output
Gifs can be created from command line without GUI, run "giffer.bat -h" for more info, it creates giffer.log containing instructions
When adding events for special dates 2.1.1 and 1.1.1 they are inserted to null dates instead
Exiting when map loading quits immediately instead of running in background till map is loaded
New way of handling tag changes, it should be now possible to change tags multiple times, create one nation several times, etc.

1.5
Batch Saves (thx to Penguintopia) - you can now select multiple saves to be replayed, they will be sorted alphabetically and information from every save above the first one will be added to the first one successively (this is necessary for technology mapmode and others that use information stored in save outside history)
Technology mapmode (thx to tinholt) in two versions (for both brighter is better):
- combined where all tech branches are combined to green color
- separate where red part of country color is military, blue diplomatic and green administrative technology level
Added support for subjects breaking free
RNW support! See separate section
Minor fixes

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
Yahiko - French localization
PreXident's Dad - cracking float5 format

################
#Used Libraries#
################
Elliot Kroo's GifSequenceWriter under Creative Commons Attribution 3.0 Unported License.
PositionInputStream by erickson.
Apache Commons Compress under Apache License
JCommander under Apache License