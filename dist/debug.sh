#!/bin/bash
java -Xms1024m -Xmx1024m -Djavafx.autoproxy.disable=true -jar replayer.jar "$@" >standard.log 2>error.log