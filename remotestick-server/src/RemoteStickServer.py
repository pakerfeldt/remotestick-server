from bottle import route, run, response, request
from ctypes import cdll, c_char_p, c_int
from uuid import *

api_keys = []
api_keys.append(str(uuid4()))
api_keys.append('520397fa-5357-4285-b37a-5dad54702a01')

print api_keys

#Device methods
TELLSTICK_TURNON = 1
TELLSTICK_TURNOFF = 2
TELLSTICK_BELL = 4
TELLSTICK_TOGGLE = 8
TELLSTICK_DIM = 16
TELLSTICK_LEARN = 32
ALL_METHODS = TELLSTICK_TURNON | TELLSTICK_TURNOFF | TELLSTICK_BELL | TELLSTICK_TOGGLE | TELLSTICK_DIM | TELLSTICK_LEARN

#Error codes
TELLSTICK_SUCCESS = 0
TELLSTICK_ERROR_NOT_FOUND = -1
TELLSTICK_ERROR_PERMISSION_DENIED = -2
TELLSTICK_ERROR_DEVICE_NOT_FOUND = -3
TELLSTICK_ERROR_METHOD_NOT_SUPPORTED = -4
TELLSTICK_ERROR_COMMUNICATION = -5
TELLSTICK_ERROR_UNKNOWN = -99

#Device typedef
TELLSTICK_TYPE_DEVICE = 1
TELLSTICK_TYPE_GROUP = 2

#Protocol Nexa
TELLSTICK_DEVICE_YCR3500 = 1
TELLSTICK_DEVICE_YCR300D = 2
TELLSTICK_DEVICE_WSR1000 = 3
TELLSTICK_DEVICE_CMR1000 = 4
TELLSTICK_DEVICE_CMR300 = 5
TELLSTICK_DEVICE_PA33300 = 6
TELLSTICK_DEVICE_EL2000 = 8
TELLSTICK_DEVICE_EL2005 = 9
TELLSTICK_DEVICE_EL2006 = 10
TELLSTICK_DEVICE_SYCR3500 = 12
TELLSTICK_DEVICE_SYCR300 = 13
TELLSTICK_DEVICE_HDR105 = 14
TELLSTICK_DEVICE_ML7100 = 15
TELLSTICK_DEVICE_EL2004 = 16
TELLSTICK_DEVICE_EL2016 = 17
TELLSTICK_DEVICE_EL2010 = 18
TELLSTICK_DEVICE_LYCR1000 = 20
TELLSTICK_DEVICE_LYCR300 = 21
TELLSTICK_DEVICE_LCMR1000 = 22
TELLSTICK_DEVICE_LCMR300 = 23
TELLSTICK_DEVICE_EL2023 = 24
TELLSTICK_DEVICE_EL2024 = 25
TELLSTICK_DEVICE_EL2021 = 26
TELLSTICK_DEVICE_EL2017 = 27
TELLSTICK_DEVICE_EL2019 = 28

#Protocol Ikea
TELLSTICK_DEVICE_KOPPLA = 19

ret = ctypes.util.find_library("TelldusCore")
if ret == None:
    print "None"
else:
    print ret
# libtelldus = cdll.LoadLibrary("libtelldus-core.so")
libtelldus = cdll.LoadLibrary("TelldusCore")
libtelldus.tdGetName.restype = c_char_p
libtelldus.tdLastSentValue.restype = c_char_p
libtelldus.tdGetProtocol.restype = c_char_p
libtelldus.tdGetModel.restype = c_char_p
libtelldus.tdGetErrorString.restype = c_char_p

def errmsg(x):
    return {
        100: "API key could not be verified.",
        101: "Name not supplied.",
        102: "Model not supplied.",
        103: "Protocol not supplied.",
        110: "Malformed parameters.",
        111: "No device removed."
    }[x]

def err(code, msg=None):
    if msg == None:
        return "<error code=\"" + str(code) + "\">" + errmsg(code) + "</error>"
    else:
        return "<error code=\"" + str(code) + "\">" + msg + "</error>"

def errxml(errors):
    retval = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<errors>\n"
    for error in errors:
        retval += "\t" + error + "\n"
    response.status = 501
    return retval + "</errors>"
    
def ok(msg=None):
    if msg != None:
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<response code=\"0\">" + msg + "</response>"
    else:
        return "<response code=\"0\">OK</response>"

def verify_api_key(request):
#    key = request.POST.get('apikey', '').strip()
    key = None
    if key == None:
        key = request.GET.get('apikey', '').strip()

    if key in api_keys:
        return True, None
    else:
        return False, "<errors>\n" + err(100) + "</errors>"

def read_device(identity):
    name = libtelldus.tdGetName(identity)
    lastcmd = libtelldus.tdLastSentCommand(identity, 1)
    protocol = libtelldus.tdGetProtocol(identity)
    model = libtelldus.tdGetModel(identity)
    methods = libtelldus.tdMethods(identity, ALL_METHODS)
    element = "<device id=\"" + str(identity) + "\">\n\t\t<name>" + name + "</name>\n\t\t<protocol>" + protocol + "</protocol>\n\t\t<model>" + model + "</model>\n"
    element += "\t\t<lastcmd>" + ("ON" if lastcmd == 1 else "OFF") + "</lastcmd>\n"
    if methods & TELLSTICK_BELL:
        element += "\t\t<supportedMethod>" + "TELLSTICK_BELL</supportedMethod>\n"
    if methods & TELLSTICK_TOGGLE:
        element += "\t\t<supportedMethod>" + "TELLSTICK_TOGGLE</supportedMethod>\n"
    if methods & TELLSTICK_TURNOFF:
        element += "\t\t<supportedMethod>" + "TELLSTICK_TURNOFF</supportedMethod>\n"
    if methods & TELLSTICK_TURNON:
        element += "\t\t<supportedMethod>" + "TELLSTICK_TURNON</supportedMethod>\n"
    if methods & TELLSTICK_DIM:
        element += "\t\t<supportedMethod>" + "TELLSTICK_DIM</supportedMethod>\n"
    if methods & TELLSTICK_LEARN:
        element += "\t\t<supportedMethod>" + "TELLSTICK_LEARN</supportedMethod>\n"
    element += "</device>\n"
    return element

@route('/')
def index():
    response.content_type = 'text/xml; charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        return err

    return '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<result>Hello World!</result>'

@route('/devices/?', method='GET')
def devices():
    response.content_type = 'text/xml; charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        return err

    result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<devices>\n"
    numDevices = libtelldus.tdGetNumberOfDevices()
    for i in range(numDevices):
        result += read_device(libtelldus.tdGetDeviceId(i))
    result += "</devices>"
    return result

@route('/devices/?', method='POST')
def new_device():
    errors = []
    response.content_type = 'text/xml; charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        return err

    name = request.POST.get('name', '').strip()
    if not name:
        errors.append(err(101))
    model = request.POST.get('model', '').strip()
    if not model:
        errors.append(err(102))
    protocol = request.POST.get('protocol', '').strip()
    if not protocol:
        errors.append(err(103))
    rawParams = request.POST.get('parameters', '').strip()
    print rawParams
    parameters = []
    if rawParams != None:
        for param in rawParams.split():
            keyval = param.split('=')
            if len(keyval) != 2:
                errors.append(err(110))
            else:
                parameters.append(keyval)

    if len(errors) > 0:
        return errxml(errors)

    identity = libtelldus.tdAddDevice()
    libtelldus.tdSetName(identity, name)
    libtelldus.tdSetProtocol(identity, protocol)
    libtelldus.tdSetModel(identity, model)
    print parameters
    for param in parameters:
        libtelldus.tdSetDeviceParameter(identity, param[0], param[1])
    return read_device(identity)

@route('/devices/:id/?', method='GET')
def get_device(id, create_header=True):
    verified, err = verify_api_key(request)
    if not verified:
        return err

    response.content_type = 'text/xml; charset=UTF-8'
    try:
        if create_header:
            retval = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        else:
            retval = ""
        retval += read_device(int(id))
        return retval
    except ValueError:
        errors = []
        errors.append(err(110))
        return errxml(errors)


@route('/devices/:id/?', method='DELETE')
def delete_device(id):
    response.content_type = 'text/xml; charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        return err

    try:
        retval = libtelldus.tdRemoveDevice(int(id))
    except ValueError:
        errors = []
        errors.append(err(110))
        return errxml(errors)
    if retval == 1:
        return ok()
    else:
        errors = []
        errors.append(err(111))
        return errxml(errors)

@route('/devices/:id/?', method='PUT')
def change_device(id):
    errors = []
    response.content_type = 'text/xml; charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        return err

    name = request.POST.get('name', '').strip()
    protocol = request.POST.get('protocol', '').strip()
    model = request.POST.get('model', '').strip()
    if not name:
        errors.append(err(101))

    if len(errors) > 0:
        return errxml(errors)
        
    libtelldus.tdSetName(int(id), name)
    libtelldus.tdSetProtocol(int(id), protocol)
    libtelldus.tdSetModel(int(id), model)
    return ok()

@route('/devices/:id/on', method='GET')
def turnon_device(id):
    response.content_type = 'text/xml, charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        response.status = 403
        return err

    retval = libtelldus.tdTurnOn(int(id))
    if retval == 0:
        return ok(libtelldus.tdGetErrorString(retval))
    else:
        errors = []
        errors.append(err(retval, libtelldus.tdGetErrorString(retval)))
        return errxml(errors)

@route('/devices/:id/off', method='GET')
def turnoff_device(id):
    response.content_type = 'text/xml, charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        response.status = 403
        return err

    retval = libtelldus.tdTurnOff(int(id))
    if retval == 0:
        return ok(libtelldus.tdGetErrorString(retval))
    else:
        errors = []
        errors.append(err(retval, libtelldus.tdGetErrorString(retval)))
        return errxml(errors)

@route('/devices/:id/dim/:level', method='GET')
def turnoff_device(id, level): #@DuplicatedSignature
    response.content_type = 'text/xml, charset=UTF-8'
    verified, err = verify_api_key(request)
    if not verified:
        return err

    errors = []
    try:
        identity = int(id)
        dimlevel = chr(int(level))
    except ValueError:
        errors.append(err(110))

    if len(errors) > 0:
        return errxml(errors)

    retval = libtelldus.tdDim(identity, dimlevel)
    if retval == 0:
        return ok(libtelldus.tdGetErrorString(retval))
    else:
        errors = []
        errors.append(err(retval, libtelldus.tdGetErrorString(retval)))
        return errxml(errors)

@route('/changedevice/')
def html_changedevice():
    response.content_type = 'text/html, charset=UTF-8'
    retval = "<html><head></head><body><form name=\"changedevice\" action=\"/devices/1\" method=\"post\"><input type=\"text\" name=\"name\"/>"
    retval += "<input type=\"text\" name=\"protocol\"/><input type=\"text\" name=\"model\"/>"
    retval += "<input type=\"submit\"/></form></body></html>"
    return retval

@route('/newdevice')
def html_new_device():
    response.content_type = 'text/html, charset=UTF-8'
    retval = "<html><head></head><body><form name=\"newdevice\" action=\"/devices\" method=\"post\">Name:<input type=\"text\" name=\"name\"/><br/>"
    retval += "Protocol:<input type=\"text\" name=\"protocol\"/><br/>Model:<input type=\"text\" name=\"model\"/><br/>"
    retval += "Parameters:<input type=\"text\" name=\"parameters\"/><br/><input type=\"submit\"/></form></body></html>"
    return retval

@route('/deletedevice/:id')
def html_delete_device(id):
    response.content_type = 'text/html, charset=UTF-8'
    retval = "<html><head></head><body><form name=\"deletedevice\" action=\"/devices/" + id + "\" method=\"delete\">"
    retval += "<input type=\"submit\"/></form></body></html>"
    return retval


run(reloader=True, port=8001)

