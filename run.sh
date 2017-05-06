#!/bin/sh


rtl_fm -f 144390000 -s 22050 -o 4 -g 100 -C - | java -jar dist/Ax25Irc.jar stdin

