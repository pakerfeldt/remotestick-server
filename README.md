# remotestick-server

## What is it?
remotestick-server exposes the Tellstick (see http://www.telldus.se) interface through RESTful services. remotestick-server uses the native library libtelldus-core to communicate with the Tellstick. It supports resource browsing and resource operations (like turning on/off a device). Responses are given in JSON format. It aims to fully reflect the capabilities of the libtelldus-core.

## Pre-requisites
telldus-core need be installed. telldus-core is an open-source library developed by the very same company manufacturing the Tellstick, namely Telldus.
Either:

 * install TelldusCenter from [http://download.telldus.se/TellStick/Software/TelldusCenter](http://download.telldus.se/TellStick/Software/TelldusCenter) (which will give you the telldus-core library as well) or 
 * compile/install telldus-core manually. telldus-core is found here [http://download.telldus.se/TellStick/Software/telldus-core/](http://download.telldus.se/TellStick/Software/telldus-core/)

## Getting it

 * A stable version of remotestick-server is found here [INSERT LINK] (recommended).
 * A bleeding edge version of remotestick-server can be grabbed from the github repository under remotestick-server/.

## Using it
The -h flag will give you help about available command line arguments:
    remotestick-server -h

Starting remotestick-server is as simple as:
    remotestick-server

By default, no API key will be used (making it possible for anyone to query your tellstick) and the RESTful services will listen for connections on your-hostname:8052.
Overriding default values is done by:
    remotestick-server -a MySecretKey -h 192.168.1.50 -p 9055
