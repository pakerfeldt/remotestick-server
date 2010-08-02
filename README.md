# remotestick-server

## What is it?
RemoteStick server exposes the Tellstick
(see [Telldus Homepage](http://www.telldus.se) ) interface through RESTful
services. RemoteStick server uses the native library libtelldus-core to
communicate with the Tellstick. It supports resource browsing and resource
operations (like turning on/off a device). Responses are given in XML format
(support for JSON format is planned). It aims to fully reflect the capabilities
of the libtelldus-core.

## Prerequisites
telldus-core need be installed. telldus-core is an open-source library developed
by the very same company manufacturing the Tellstick, namely Telldus.
Either:

 * install TelldusCenter from
 [http://download.telldus.se/TellStick/Software/TelldusCenter](http://download.telldus.se/TellStick/Software/TelldusCenter)
 (which will give you the telldus-core library as well) or 
 * compile/install telldus-core manually. telldus-core is found here
 [http://download.telldus.se/TellStick/Software/telldus-core/](http://download.telldus.se/TellStick/Software/telldus-core/)

## Getting it

 * A stable version of RemoteStick Server is found here
 http://github.com/pakerfeldt/remotestick/downloads (recommended).
 * A bleeding edge version of RemoteStick Server can be grabbed from the github
 repository remotestick.

## Using it
The -? flag will give you help about available command line arguments:
    remotestick-server.py -?

Starting RemoteStick server is as simple as (although not recommended, see
below):
    remotestick-server.py

By default, no authentication will be required (making it possible for anyone to
query your tellstick) and the RESTful services will listen for connections on
your-hostname:8422. Only Basic Authentication (HTTP) currently supported.
You should at least set a username and password, requiring client
authentication:
    remotestick-server.py --username MyUsername --password MyPassword

## Troubleshooting

### Linux
Depending on where the telldus-core library is installed on your system you may
have to define the environment variable LD_LIBRARY_PATH to include the directory
where telldus-core is located.
    export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:/opt/telldus/bin

### Windows
If TelldusCenter is installed RemoteStick server will have no problem locating
the library. Although you might get an error saying ftdxx library is not found.
In such cases add the Telldus folder to the PATH environment variable.
    set PATH=%PATH%;"C:\Program files\Telldus" 
or try running RemoteStick server using Telldus folder as working directory
(i.e. stand in that directory when starting RemoteStick server).

### Mac OS X
The current version of TelldusCenter for Mac OS X, 2.0.2, does only come with 32
bit support meaning that it will not work (out of the box) if you got a 64 bit
version of Python (which you do if you're running a 64 bit version of Mac OS X,
i.e. Snow Leopard). The error you might get indicating this is:
    OSError: dlopen(/Library/Frameworks/TelldusCore.framework/TelldusCore, 6):
    no suitable image found.
    Did find:/Library/Frameworks/TelldusCore.framework/TelldusCore: mach-o, but
    wrong architecture