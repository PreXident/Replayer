########################
#EU4 save game replayer#
########################

This is a fan WiP project coded in free time and originally intended for my personal use only as JavaFX exercise, so there's no warranty, use only on your own risk! You will probably need at least jre7.
Before starting, set "eu4.dir" property in file "replayer.properties" to directory containing the game. Property "days.per.tick" controls the speed of the replay.
There's a bug in javafx which sometimes prevents the map from refreshing. If this is your case and Settings->Emergency Refresh does not help you, download jre7 update 40 or newer; or backup provinces.bmp and experiment with its size (for me the width has to be 4096 pixels or less, it likely depends on your graphic card).
If you mess with (un)checking controller or owner events during replaying, you may get weird results.

Naturally the more event types you want to track, the slower the replaying is.

Tested on only one machine and one complete vanilla save started in 1444.11.11, so it may not work properly with yours.
Submit any bugs, problems and feature requests on Paradox forums or PM me (nick PreXident), maybe we can figure it out.

############
#Used files#
############

map/default.map
map/provinces.bmp
map/definition.csv
common/country_tags/* + files mentioned in there (typically common/countries/*)

############
#Known Bugs#
############

In province history there's sometimes missing record of regaining province control from rebels. So in the end there are more rebel controlled provinces than there should be. If you have any idea why this is happening or how to detect this, please share!

##################
#Planned features#
##################

From realistic ones to dreams:
- fast forward replay to any given date, not only end date
- display info when hovering over provinces
- gif export?
- monarchs, wars, ...
- binary savegame (ironman)

############
#Change log#
############

0.2
Setting the speed of replay in more user-friendly way

0.1
Initial release

#########
#Sources#
#########

If anyone is interested or paranoid enough, sources are freely available, just PM me. However they are no masterpiece I am afraid ;-) It's a netbeans free form project, so you just need to adjust properties in build.xml if JAVA_HOME is not set and then use ant.