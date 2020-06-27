<a name="top"></a>

| Title |Component |Priority| Id |
|---|---|---|---|
|Look into compatibility with Extended Timeline mod |  | major |<a href='#anchor_4'>4</a>|
|Add support for debug_saves and technologies/economy/tradevalue mapmodes |  | major |<a href='#anchor_10'>10</a>|
|Add Mod Selector |  | major |<a href='#anchor_13'>13</a>|
|Hierarchical vassals as overlords |  | major |<a href='#anchor_9'>9</a>|
|Add support for localization and internationalization |  | major |<a href='#anchor_3'>3</a>|
|Changing tags more complicated |  | major |<a href='#anchor_12'>12</a>|
|Look at the possibility to parse game.log |  | major |<a href='#anchor_5'>5</a>|
|Add RNW support |  | major |<a href='#anchor_2'>2</a>|
|Better gif output |  | major |<a href='#anchor_1'>1</a>|
|Parsing wars |  | minor |<a href='#anchor_8'>8</a>|
|Display battles |  | minor |<a href='#anchor_11'>11</a>|
|Parsing monarchs |  | minor |<a href='#anchor_7'>7</a>|
|Decode binary saves |  | trivial |<a href='#anchor_6'>6</a>|



<a name="anchor_4"></a>
## Issue 4 - Look into compatibility with Extended Timeline mod

Look into compatibility with Extended Timeline mod

| Field | Value |
|---|---|
|Component||
|Kind|bug|
|Status|new|
|Priority|major|
|Created|2014-02-21 13:59:52.964813 +0000 +0000|
|Updated|2014-02-21 13:59:52.964813 +0000 +0000|


### Details

When using Extented Timeline mod strange things happen in New World. Colonials control the area too soon etc.




------------------
<a href='#top'>top<a>

<a name="anchor_10"></a>
## Issue 10 - Add support for debug_saves and technologies/economy/tradevalue mapmodes

Add support for debug_saves and technologies/economy/tradevalue mapmodes

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|on hold|
|Priority|major|
|Created|2014-02-26 21:25:38.015928 +0000 +0000|
|Updated|2014-03-03 15:25:05.61626 +0000 +0000|


### Details

Feature debug_saves in settings.txt creates multiple autosaves without overwriting.
It would be nice to combine this feature with information stored without dates (eg technologies, economy and trade value mapmodes). For now gif output is sufficient.



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-03 15:12:08.744781 +0000 +0000|

Commits:  
a84eb60 - First version of batch save game parser  
380e2fc - Added support for subjects breaking free  
13f3807 - Fixed exception when setting title in parsers  
4975f72 - Added technology mapmode  
d8cc776 - From now both technology maps are available (separate and combined display)  



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-03 15:22:56.553626 +0000 +0000|

Economy and trade value mapmodes are suspended as actual values shown in game are not stored in save games but calculated from unknown formula



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-03 15:25:05.646704 +0000 +0000|

Other mapmodes require additional substantial work as modifiers, governments, stability, trade goods (support/demand) etc would be needed to preview them




------------------
<a href='#top'>top<a>

<a name="anchor_13"></a>
## Issue 13 - Add Mod Selector

Add Mod Selector

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|resolved|
|Priority|major|
|Created|2014-04-27 06:15:44.844228 +0000 +0000|
|Updated|2014-04-27 22:02:23.336313 +0000 +0000|


### Details

Add way to allow users select mods without editing replayer.properties. Probably use simple standalone app as EU4 does.



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-04-27 22:02:23.331059 +0000 +0000|

Added ModSelector, so users do not need to edit replayer.properties for setting mod.list

Fixes #13

→ <<cset 6dcd60dcb9c0>>




------------------
<a href='#top'>top<a>

<a name="anchor_9"></a>
## Issue 9 - Hierarchical vassals as overlords

Hierarchical vassals as overlords

| Field | Value |
|---|---|
|Component||
|Kind|bug|
|Status|resolved|
|Priority|major|
|Created|2014-02-22 08:52:40.297034 +0000 +0000|
|Updated|2014-02-26 21:21:23.117339 +0000 +0000|


### Details

Vassals as overlords feature does not handle vassals of junior partners in PUs etc. well. They should be displayed as part of the top overlord.



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-02-26 21:21:23.112209 +0000 +0000|

Feature subjects.as.overlord can now handle hierarchical subjects

Fixes #9

→ <<cset 45b524e37387>>




------------------
<a href='#top'>top<a>

<a name="anchor_3"></a>
## Issue 3 - Add support for localization and internationalization

Add support for localization and internationalization

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|resolved|
|Priority|major|
|Created|2014-02-21 13:05:08.862934 +0000 +0000|
|Updated|2014-03-03 15:02:37.851247 +0000 +0000|


### Details

Think of a nice way to localize the GUI



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-03 15:01:59.653092 +0000 +0000|

Commits:  
0fbd2ec - First part of localization  
4757e30 - Better name for localization property file  
2935edc - Missing localization is indicated by ${...}  
ec20350 - Next wave of localizations  
0accef9 - Next wave of localizations  
d032082 - Next wave of localization  
0c52705 - Next wave of localization  
fd99d24 - Loading period.per.tick from settings adapted for new storing method  




**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-03 15:02:37.885957 +0000 +0000|

Done!




------------------
<a href='#top'>top<a>

<a name="anchor_12"></a>
## Issue 12 - Changing tags more complicated

Changing tags more complicated

| Field | Value |
|---|---|
|Component||
|Kind|bug|
|Status|resolved|
|Priority|major|
|Created|2014-04-06 12:07:44.409035 +0000 +0000|
|Updated|2014-04-25 15:22:39.165899 +0000 +0000|


### Details

Command change_tag transfers country history, which may lead to errors in replaying and also information loss  
See [this](http://forum.paradoxplaza.com/forum/showthread.php?708855-Quick-questions-thread&p=17196424&viewfull=1#post17196424)   
The whole tag change logic needs more research and rethinking with considering the strange history records related to it



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-04-25 15:22:17.988974 +0000 +0000|

Related commits:  
376ad5c - First prototype of new tag changes handling  
f610253 - New tag changes now handles controller changes too  



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-04-25 15:22:39.182924 +0000 +0000|

Done!




------------------
<a href='#top'>top<a>

<a name="anchor_5"></a>
## Issue 5 - Look at the possibility to parse game.log

Look at the possibility to parse game.log

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|new|
|Priority|major|
|Created|2014-02-22 08:28:01.913755 +0000 +0000|
|Updated|2014-02-22 08:28:01.913755 +0000 +0000|


### Details

Log file game.log could contain very useful information. However it may not be as reliable as desired and of course some kind of following will be needed as it's recreated each run.
Maybe users can start a follower app each time they play to propagate changes to more permanent file that could be used along with save for replaying?

http://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/input/Tailer.html




------------------
<a href='#top'>top<a>

<a name="anchor_2"></a>
## Issue 2 - Add RNW support

Add RNW support

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|resolved|
|Priority|major|
|Created|2014-02-21 13:01:13.314303 +0000 +0000|
|Updated|2014-03-09 12:18:20.161651 +0000 +0000|


### Details

Create mod generator for exporting screenshot mimicking provinces.bmp to support Random New World from Conquest of Paradise



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-09 12:17:41.589885 +0000 +0000|

Commits:  
a8df136 - Added first prototype of mod generator for supporting RNW  
d708f83 - Added support for RNW.



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-09 12:18:20.178176 +0000 +0000|

Done!




------------------
<a href='#top'>top<a>

<a name="anchor_1"></a>
## Issue 1 - Better gif output

Better gif output

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|resolved|
|Priority|major|
|Created|2014-02-21 12:58:09.600843 +0000 +0000|
|Updated|2014-04-25 15:39:09.848514 +0000 +0000|


### Details

Create independent gif outputting component and add GUI to control it



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-03 15:26:50.155995 +0000 +0000|

Could be combined with overall ReplayerController overhaul



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-03-17 09:33:13.683696 +0000 +0000|

The gif submenu could look like this:  
loop  
duration  
break  
date draw  
date color  
date size  
date x  
date y  
subimage  
subimage x  
subimage y  
subimage height  
subimage width  



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-04-25 15:38:50.334852 +0000 +0000|

Related commits:  
a0e7ea6 - Added separate class for gif outputting  
605de4a - Added first few GUI elements to control gif output  
83849e9 - Added the rest of GUI for controlling gif output  
d0db7f9 - Package cleanup, part of ReplayerController overhaul  
bee260a - WiP: separating GUI from logic  
abfaaa4 - WiP: separating GUI from logic  
51fe292 - WiP: separating GUI from logic  
4ccd882 - WiP: separating GUI from logic, loading saves done  
252bbba - WiP: separating GUI from logic, loading data moved to Replay  
ac63f7d - Added option to create gifs from command line  
7865884 - Giffer can now create multiple mapmode gifs at once and load all eu4 saves from a specified directory  
609f7c6 - Got rid of unnecessary references to javafx.beans.value.WritableValue, so java7 can easily run giffer.bat  
2fd109d - Added forgotten file from the previous commit



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-04-25 15:39:09.873869 +0000 +0000|

Done!




------------------
<a href='#top'>top<a>

<a name="anchor_8"></a>
## Issue 8 - Parsing wars

Parsing wars

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|new|
|Priority|minor|
|Created|2014-02-22 08:48:55.951685 +0000 +0000|
|Updated|2014-02-22 08:48:55.951685 +0000 +0000|


### Details

If country GUI (like for monarch issue #6) is created, the wars could be parse and displayed too. Both current and previous ones.




------------------
<a href='#top'>top<a>

<a name="anchor_11"></a>
## Issue 11 - Display battles

Display battles

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|new|
|Priority|minor|
|Created|2014-03-09 12:21:31.570552 +0000 +0000|
|Updated|2014-03-09 12:21:31.570552 +0000 +0000|


### Details

Look into possibility of displaying battles on the map as described here:  
http://forum.paradoxplaza.com/forum/showthread.php?722493-UTILITY-Java-Save-Game-Replayer&p=16980999&viewfull=1#post16980999  
It is related to parsing wars (Issue #8)




------------------
<a href='#top'>top<a>

<a name="anchor_7"></a>
## Issue 7 - Parsing monarchs

Parsing monarchs

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|new|
|Priority|minor|
|Created|2014-02-22 08:47:03.909617 +0000 +0000|
|Updated|2014-03-03 15:27:28.637507 +0000 +0000|


### Details

Extend the replayer to parse the monarch information from saves to display PUs and current monarchs of countries. This probably needs new GUI features.



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-02-22 08:49:47.60483 +0000 +0000|

Do not forget to adapt nation focus feature to display PUs.




------------------
<a href='#top'>top<a>

<a name="anchor_6"></a>
## Issue 6 - Decode binary saves

Decode binary saves

| Field | Value |
|---|---|
|Component||
|Kind|enhancement|
|Status|resolved|
|Priority|trivial|
|Created|2014-02-22 08:33:45.329542 +0000 +0000|
|Updated|2014-04-25 15:28:41.68262 +0000 +0000|


### Details

Decoding the ironman binary saves is the Holy Grail of replaying...
Unfortunately all efford may be in vain as CK2 saves seem to be encrypted now and this may happen to EU4 as well.



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-02-22 08:43:42.994198 +0000 +0000|

Despair not - the CK2 saves are only zipped!



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-04-25 15:26:41.17027 +0000 +0000|

Related commits:  
7173e97 - Added support for binary saves and standalone converter  
a931243 - Attempt to make the binary to text conversion faster  
cb29436 - IronmanStream is now more tolerant to unknown tokens and converts them to UNKNOWN_0xC0DE instead of throwing an exception  
0b0564a - TokenInfos are no longer hardcoded, but stored in tokens.csv  
22d6418 - Deleted commented out code 



**Comment**

| Field | Value |
|---|---|
|**By:**|  |
|**Created:** | 2014-04-25 15:28:41.718483 +0000 +0000|

Done!




------------------
<a href='#top'>top<a>


