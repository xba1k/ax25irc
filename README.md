# ax25irc

ax25irc is a Java project attempting to create an IRC-like messaging system over
HAM radio. This software can be used by both licensed HAMs and general public,
though only licensed HAMs would be legally allowed to transmit.

ax25irc is not implemented from scratch. It combines following projects and libraries:

* https://github.com/Harha/mirage-ircd
* https://github.com/ab0oo/javAPRSlib
* https://github.com/sivantoledo/javAX25
* https://github.com/amishHammer/libkisstnc-java

Those codebases have been merged into the project for ease of development.
Currently RTL-SDR (receive only), Mobilinkd and TNC-X have been tested on Ubuntu and OSX.

## Build

You will need Java 8 (JDK) and Apache Ant to build this project. Simply run build.sh
if you're on Linux or OSX, or run ant manually.

This project depends on RXTX library to perform serial port communication with TNC.
Ubuntu provides RXTX via standard package repository. OSX version is available
here : http://jlog.org/rxtx-mac.html.

## Run

Three application modes are available :

1. stdin

   AX25 packets are decoded from audio stream via standard input. This mode can be used
with RTL-SDR via rtl_fm as demonstrated in provided run.sh script.

2. rtlfm

   Application will directly invoke rtl_fm via shell. This mode will not work on Windows.  
Invoke as **java -jar dist/Ax25Irc.jar rtlfm**.

3. kiss

   Application will attempt to open a serial port for communication with a KISS compatible
TNC.  
Invoke as  **java -jar dist/Ax25Irc.jar kiss <path to serial port>**

## Use

Once the application is running, it will listen on standard IRC TCP port 6667. Users can
connect with any IRC compatible client with no password. Licensed HAM operators
intending to transmit should utilize their callsign as username, with or without SSID.

Once connected, users can join one or several of the pre-populated channels :

* #APRS - displays all received APRS packets in decoded form. Posting to channel has no effect.
* #APRS-RAW - displays received APRS packets in raw form. Posting to channel has no effect.
* #APRS-CHAT - channel will display APRS Message packets when target callsign is 1 character long.
Posting to channel will transmit APRS Message to target callsign "A".
* #AX25-CHAT - channel displays non-APRS AX25 payloads.
* #CONTROL - channel for manipulating runtime configuration.

In addition, direct messages sent to any callsign will be sent as APRS Messages,
unless global mode is set to AX25.

## Screenshots

![#APRS screenshot](https://github.com/xba1k/ax25irc/#APRS%20screenshot.png)
![#APRS-RAW screenshot](https://github.com/xba1k/ax25irc/#APRS-RAW%20screenshot.png)
