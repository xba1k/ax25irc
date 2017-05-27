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
Currently RTL-SDR (receive only), Mobilinkd and TNC-X have been tested on Ubuntu, OSX and Windows.

## Build

You will need Java 8 (JDK) and Apache Ant to build this project. Simply run build.sh
if you're on Linux or OSX, or run ant manually.

The project depends on jSerialComm library for serial communication to KISS TNC.
http://fazecast.github.io/jSerialComm/

## Run

Multiple application modes are available :

1. stdin

   AX25 packets are decoded from audio stream via standard input. This mode can be used
with RTL-SDR via rtl_fm as demonstrated in provided run.sh script.

2. rtlfm

   Application will directly invoke rtl_fm via shell. This mode will not work on Windows.  
Invoke as **java -jar Ax25Irc.jar rtlfm**.  

3. kiss

   Application will attempt to open a serial port for communication with a KISS compatible
TNC.  
Invoke as  **java -jar Ax25Irc.jar kiss &lt;path to serial port&gt;**  

4. sound

   Application will process input/output on the standard sound device. Theoretically this
   can be used with any radio in VOX mode, but I have only tested this mode only for decoding
   so far. Also sound card naming might be OS-specific, this still needs to be verified on Ubuntu and Windows.  
   Invoke as **java -jar Ax25Irc.jar sound**  

## Use

Once the application is running, it will listen on standard IRC TCP port 6667. Users can
connect with any IRC compatible client with no password. Licensed HAM operators
intending to transmit should utilize their callsign as username, with or without SSID.

Once connected, users can join one or several of the pre-populated channels :

* #APRS - displays all received APRS packets in decoded form. Posting to channel has no effect.
* #APRS-RAW - displays received APRS packets in raw form. Posting to channel has no effect.
* #APRS-CHAT - channel will display APRS Message packets when target call sign is 1 character long.
Posting to channel will transmit APRS Message to target call sign "A".
* #AX25-CHAT - channel displays non-APRS AX25 payloads.
* #CONTROL - channel for manipulating runtime configuration.

In addition, direct messages sent to any call sign will be sent as APRS Messages,
unless global mode is set to AX25. Note that call sign "A" for "round table" communication
was chosen arbitrarily and can be replaced with any other string that is not a real
call sign.

## Screenshots

![#APRS screenshot](https://github.com/xba1k/ax25irc/blob/master/%23APRS%20screenshot.png)
![#APRS-RAW screenshot](https://github.com/xba1k/ax25irc/blob/master/%23APRS-RAW%20screenshot.png)
