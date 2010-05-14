# remotestick-server

## What is it?
remotestick-server exposes the Tellstick (see [Telldus Homepage](http://www.telldus.se) ) interface through RESTful services. remotestick-server uses the native library libtelldus-core to communicate with the Tellstick. It supports resource browsing and resource operations (like turning on/off a device). Responses are given in XML format (support for JSON format is planned). It aims to fully reflect the capabilities of the libtelldus-core.

## Prerequisites
telldus-core need be installed. telldus-core is an open-source library developed by the very same company manufacturing the Tellstick, namely Telldus.
Either:

 * install TelldusCenter from [http://download.telldus.se/TellStick/Software/TelldusCenter](http://download.telldus.se/TellStick/Software/TelldusCenter) (which will give you the telldus-core library as well) or 
 * compile/install telldus-core manually. telldus-core is found here [http://download.telldus.se/TellStick/Software/telldus-core/](http://download.telldus.se/TellStick/Software/telldus-core/)

## Getting it

 * A stable version of remotestick-server is found here http://github.com/pakerfeldt/remotestick/downloads (recommended).
 * A bleeding edge version of remotestick-server can be grabbed from the github repository remotestick.

## Using it
The -? flag will give you help about available command line arguments:
    remotestick-server.py -?

Starting remotestick-server is as simple as (although not recommended, see below):
    remotestick-server.py

By default, no authentication will be required (making it possible for anyone to query your tellstick) and the RESTful services will listen for connections on your-hostname:8080. Only Basic Authentication (HTTP) currently supported.
You should at least set a username and password, requiring client authentication:
    remotestick-server.py --username MyUsername --password MyPassword
