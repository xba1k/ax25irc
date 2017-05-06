# ax25irc

ax25irc is a Java project attempting to create an IRC-like messaging system over
HAM radio. This software can be used by both licensed HAMs and general public,
though only licensed HAMs would be legally allowed to transmit.

ax25irc is not implemented from scratch. It combines following projects and libraries:

https://github.com/Harha/mirage-ircd
https://github.com/ab0oo/javAPRSlib
https://github.com/sivantoledo/javAX25
https://github.com/amishHammer/libkisstnc-java

Those codebases have been merged into the project for easy of development.
Currently RTL-SDR and Mobilinkd have been tested on Ubuntu and OSX.
As usual, this is the kind of project where work is always in progress, stay tuned.
