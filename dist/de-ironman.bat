@echo off
java -Xms1024m -Xmx1024m -cp ".;replayer.jar" com.paradoxplaza.eu4.replayer.parser.savegame.binary.Converter -d %*
pause