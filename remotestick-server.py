#!/usr/bin/env python

#
#   Copyright 2010 Patrik Akerfeldt
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
# 
# 

from bottle import route, run, response, request
from ctypes import util
from ctypes import *
from getopt import getopt, GetoptError
from sys import argv, exit, platform
from base64 import b64encode
import time

VERSION = "0.3.5"
API_VERSION = 1

#Device methods
TELLSTICK_TURNON = 1
TELLSTICK_TURNOFF = 2
TELLSTICK_BELL = 4
TELLSTICK_TOGGLE = 8
TELLSTICK_DIM = 16
TELLSTICK_LEARN = 32
ALL_METHODS = TELLSTICK_TURNON | TELLSTICK_TURNOFF | TELLSTICK_BELL | TELLSTICK_TOGGLE | TELLSTICK_DIM | TELLSTICK_LEARN

reqauth = True
username = None
password = None
libtelldus = None

def loadlibrary(libraryname=None):
    if libraryname == None:
        if platform == "darwin" or platform == "win32":
            libraryname = "TelldusCore"
        elif platform == "linux2":
            libraryname = "telldus-core"
        else:
            libraryname = "TelldusCore"
        ret = util.find_library(libraryname)
    else:
        ret = libraryname
    
    if ret == None:
        return (None, libraryname)

    global libtelldus
    if platform == "win32":
        libtelldus = windll.LoadLibrary(ret)
    else:
        libtelldus = cdll.LoadLibrary(ret)
    libtelldus.tdGetName.restype = c_char_p
    libtelldus.tdLastSentValue.restype = c_char_p
    libtelldus.tdGetProtocol.restype = c_char_p
    libtelldus.tdGetModel.restype = c_char_p
    libtelldus.tdGetErrorString.restype = c_char_p
    libtelldus.tdLastSentValue.restype = c_char_p

    return ret, libraryname

def errmsg(x):
    return {
        100: "Authentication failed",
        101: "Unsupported format",
        201: "Name not supplied",
        202: "Model not supplied",
        203: "Protocol not supplied",
        210: "Malformed parameters",
        211: "No device removed",
        220: "Method not supported",
        300: "Telldus-core error"
    }[x]
   
def err(format, responsecode, request, code, code_msg=None):
    response.status = responsecode
    if responsecode == 401:
        response.headers.append("WWW-Authenticate", "Basic realm=\"RemoteStick\"")

    if code_msg == None:
        code_msg = errmsg(code)
    
    if format == "xml":
        return err_xml(request, code_msg)
    else:
        return err_xml(request, code_msg)

def err_xml(request, msg):
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<hash>\n\t<request>" + request + "</request>\n\t<error>" + msg + "</error>\n</hash>"

def authenticate(auth):
    global username, password
    if reqauth and auth == None:
        return False
    elif reqauth:
        sentUsername, sentPassword = auth
        return (username == sentUsername and password == sentPassword)
    else:
        return True

def read_device(identity):
    name = libtelldus.tdGetName(identity)
    lastcmd = libtelldus.tdLastSentCommand(identity, 1)
    protocol = libtelldus.tdGetProtocol(identity)
    model = libtelldus.tdGetModel(identity)
    methods = libtelldus.tdMethods(identity, ALL_METHODS)
    lastValue = libtelldus.tdLastSentValue(identity)
    element = "<device id=\"" + str(identity) + "\">\n\t\t<name>" + name + "</name>\n\t\t<protocol>" + protocol + "</protocol>\n\t\t<model>" + model + "</model>\n"
    if lastcmd == 1:
        element += "\t\t<lastcmd>ON</lastcmd>\n"        
    else:
        element += "\t\t<lastcmd>OFF</lastcmd>\n"
    if lastValue != None and lastValue != "":
        try:
            lastValueConverted = int(lastValue)
            element += "\t\t<lastvalue>" + str(lastValueConverted) + "</lastvalue>\n"
        except:
            pass
    
    if methods & TELLSTICK_BELL:
        element += "\t\t<supportedMethod id=\"" + str(TELLSTICK_BELL) + "\">" + "TELLSTICK_BELL</supportedMethod>\n"
    if methods & TELLSTICK_TOGGLE:
        element += "\t\t<supportedMethod id=\"" + str(TELLSTICK_TOGGLE) + "\">" + "TELLSTICK_TOGGLE</supportedMethod>\n"
    if methods & TELLSTICK_TURNOFF:
        element += "\t\t<supportedMethod id=\"" + str(TELLSTICK_TURNOFF) + "\">" + "TELLSTICK_TURNOFF</supportedMethod>\n"
    if methods & TELLSTICK_TURNON:
        element += "\t\t<supportedMethod id=\"" + str(TELLSTICK_TURNON) + "\">" + "TELLSTICK_TURNON</supportedMethod>\n"
    if methods & TELLSTICK_DIM:
        element += "\t\t<supportedMethod id=\"" + str(TELLSTICK_DIM) + "\">" + "TELLSTICK_DIM</supportedMethod>\n"
    if methods & TELLSTICK_LEARN:
        element += "\t\t<supportedMethod id=\"" + str(TELLSTICK_LEARN) + "\">" + "TELLSTICK_LEARN</supportedMethod>\n"
    element += "</device>\n"
    return element

def pre_check(format, accepted_formats):
    if format not in accepted_formats:
        return False, 400, 101
    if not authenticate(request.auth):
        return False, 401, 100
    return True, None, None

def set_headers(format):
    if format == "xml":
        response.set_content_type('text/xml; charset=utf8')
    response.headers.append("X-API-VERSION", str(API_VERSION))
    response.headers.append("X-VERSION", VERSION)

@route('/devices.:format', method='GET')
def devices(format):
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, 'GET /devices.' + format, error_code)
    set_headers(format)
    result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<devices>\n"
    numDevices = libtelldus.tdGetNumberOfDevices()
    for i in range(numDevices):
        result += read_device(libtelldus.tdGetDeviceId(i))
    result += "</devices>"
    return result

@route('/devices.:format', method='POST')
def new_device(format):
    request_str = 'POST /devices.' + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)
    
    name = request.POST.get('name', '').strip()
    if not name:
        return err(format, 400, request_str, 201)

    model = request.POST.get('model', '')
    if not model:
        return err(format, 400, request_str, 202)

    protocol = request.POST.get('protocol', '')
    if not protocol:
        return err(format, 400, request_str, 203)
        
    rawParams = request.POST.get('parameters', '')
    parameters = []
    if rawParams != None:
        for param in rawParams.split():
            keyval = param.split('=')
            if len(keyval) != 2:
                return err(format, 400, request_str, 210)
            else:
                parameters.append(keyval)
    identity = libtelldus.tdAddDevice()
    libtelldus.tdSetName(identity, name.strip())
    libtelldus.tdSetProtocol(identity, protocol.strip())
    libtelldus.tdSetModel(identity, model.strip())
    for param in parameters:
        libtelldus.tdSetDeviceParameter(identity, param[0], param[1])
    retval = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    retval += read_device(identity)
    return retval

@route('/devices/:id.:format', method='GET')
def get_device(id, format):
    request_str = 'GET /devices/' + id + "." + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)

    retval = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    try:
        retval += read_device(int(id))
        return retval
    except ValueError:
        return err(format, 400, request_str, 210)


@route('/devices/:id.:format', method='DELETE')
def delete_device(id, format):
    request_str = 'DELETE /devices/' + id + "." + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)

    try:
        retval = libtelldus.tdRemoveDevice(int(id))
    except ValueError:
        return err(format, 400, request_str, 210)

    if retval == 1:
        return ""
    else:
        return err(format, 400, request_str, 211)

@route('/devices/:id.:format', method='PUT')
def change_device(id, format):
    request_str = 'PUT /devices/' + id + "." + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)

    name = request.POST.get('name', '').strip()
    protocol = request.POST.get('protocol', '').strip()
    model = request.POST.get('model', '').strip()
    if name:
        libtelldus.tdSetName(int(id), name)
    
    if model:
        libtelldus.tdSetModel(int(id), model)
    
    if protocol:
        libtelldus.tdSetProtocol(int(id), protocol)
          
    retval = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    try:
        retval += read_device(int(id))
        return retval
    except ValueError:
        return err(format, 400, request_str, 210)
    return ""

@route('/devices/:id/on.:format', method='GET')
def turnon_device(id, format):
    request_str = 'GET /devices/' + id + "/on." + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)
    
    try:
        identity = int(id)
    except ValueError:
        return err(format, 400, request_str, 210)
    
    if libtelldus.tdMethods(identity, TELLSTICK_TURNON) & TELLSTICK_TURNON:    
        retval = libtelldus.tdTurnOn(identity)
        if retval == 0:
            return ""
        else:
            return err(format, 502, request_str, 300, libtelldus.tdGetErrorString(retval))
    else:
        return err(format, 400, request_str, 220)

@route('/devices/:id/off.:format', method='GET')
def turnoff_device(id, format):
    request_str = 'GET /devices/' + id + "/off." + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)

    try:
        identity = int(id)
    except ValueError:
        return err(format, 400, request_str, 210)
    
    if libtelldus.tdMethods(identity, TELLSTICK_TURNOFF) & TELLSTICK_TURNOFF:    
        retval = libtelldus.tdTurnOff(identity)
        if retval == 0:
            return ""
        else:
            return err(format, 502, request_str, 300, libtelldus.tdGetErrorString(retval))
    else:
        return err(format, 400, request_str, 220)

@route('/devices/:id/dim/:level.:format', method='GET')
def dim_device(id, level, format):
    request_str = 'GET /devices/' + id + "/dim/" + level + "." + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)

    try:
        identity = int(id)
        dimlevel = int(level)
#        dimlevel = int(round(int(level)*2.55))
    except ValueError:
        return err(format, 400, request_str, 210)

    if libtelldus.tdMethods(identity, TELLSTICK_DIM) & TELLSTICK_DIM:    
        retval = libtelldus.tdDim(identity, dimlevel)
        if retval == 0:
            return ""
        else:
            return err(format, 502, request_str, 300, libtelldus.tdGetErrorString(retval))
    else:
        return err(format, 400, request_str, 220)
    
@route('/devices/:id/learn.:format', method='GET')
def learn_device(id, format):
    request_str = 'GET /devices/' + id + "/learn." + format
    ok, response_code, error_code = pre_check(format, ["xml"])
    if not ok:
        return err(format, response_code, request_str, error_code)
    set_headers(format)

    try:
        identity = int(id)
    except ValueError:
        return err(format, 400, request_str, 210)
    
    if libtelldus.tdMethods(identity, TELLSTICK_LEARN) & TELLSTICK_LEARN:    
        retval = libtelldus.tdLearn(identity)
        if retval == 0:
            return ""
        else:
            return err(format, 502, request_str, 300, libtelldus.tdGetErrorString(retval))
    else:
        return err(format, 400, request_str, 220)
        
def usage():
    print "Usage: remotestick-server [OPTION]..."
    print "Expose tellstick services through RESTful services."
    print ""
    print "Without any arguments remotestick-server will start a http server on 127.0.0.1:8422 where no authentication is required."
    print "Setting the name of the telldus-core library should not be needed. remotestick-server is able to figure out the correct library name automatically. If, for some reason, this is unsuccessful, use --library."
    print ""
    print "-h, --host\t\thost/IP which the server will bind to, default to loopback"
    print "-p, --port\t\tport which the server will listen on, default to 8422"
    print "-u, --username\t\tusername used for client authentication"
    print "-s, --password\t\tpassword used for client authentication"
    print "-l, --library\t\tname of telldus-core library"

def version():
    print "remotestick-server v" + VERSION

def main():
    try:
        opts, args = getopt(argv[1:], "?h:p:u:s:l:V", ["?", "host=", "port=", "username=", "password=", "library=", "version"])
    except GetoptError, err:
        print str(err)
        usage()
        exit(2)
    host = None
    port = None
    library = None
    global username
    global password
    global reqauth
    
    for o, a in opts:
        if o in ("-h", "--host"):
            host = a
        elif o in ("-p", "--port"):
            port = a
        elif o in ("-u", "--username"):
            username = a
        elif o in ("-s", "--password"):
            password = a
        elif o in ("-l", "--library"):
            library = a
        elif o in ("-V", "--version"):
            version()
            exit()
        elif o == '-?':
            usage()
            exit()
        else:
            assert False, "unhandled option " + o
    
    lib, libname = loadlibrary(library)
    if lib == None:
        print "Error: Cannot find library " + libname
        exit(3)
        
    if username == None or password == None:
        print "Warning: No authentication required. Please consider setting --username and --password."
        reqauth = False
        
    if (host == None and port == None):
        run(port="8422")
    elif host != None and port == None:
        run(host=host, port="8422")
    elif host == None and port != None:
        run(port=port)
    else:
        run(host=host, port=port)
             
if __name__ == "__main__":
    main()
