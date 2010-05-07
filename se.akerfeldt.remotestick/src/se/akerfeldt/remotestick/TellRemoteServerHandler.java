package se.akerfeldt.remotestick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class TellRemoteServerHandler extends DefaultHandler {

	private boolean inDevice;
	private List<Device> devices;
	private StringBuilder characters;
	private String deviceName;
	private boolean readCharacters;
	private String deviceProtocol;
	private String deviceModel;
	private Map<String, String> deviceParameters;
	private Integer deviceId;
	private DeviceCommand deviceLastCommand;
	private String request;
	private String error;
	private List<Integer> supportedMethods;

	public TellRemoteServerHandler() {
		devices = new ArrayList<Device>();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if (readCharacters) {
			for (int i = start; i < start + length; i++)
				characters.append(ch[i]);
		}

		// System.out.print("Characters:    \"");
		// for (int i = start; i < start + length; i++) {
		// switch (ch[i]) {
		// case '\\':
		// System.out.print("\\\\");
		// break;
		// case '"':
		// System.out.print("\\\"");
		// break;
		// case '\n':
		// System.out.print("\\n");
		// break;
		// case '\r':
		// System.out.print("\\r");
		// break;
		// case '\t':
		// System.out.print("\\t");
		// break;
		// default:
		// System.out.print(ch[i]);
		// break;
		// }
		// }
		// System.out.print("\"\n");

	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
		// System.out.println("TellRemoteServerHandler.startDocument()");
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if ("device".equals(localName)) {
			inDevice = true;
			supportedMethods = new ArrayList<Integer>();
			try {
				deviceId = Integer.valueOf(attributes.getValue("id"));
			} catch (NumberFormatException nfe) {
				throw new SAXException(
						"id attribute of device element is not a number", nfe);
			}
			deviceParameters = new HashMap<String, String>();
		} else if (inDevice) {
			if ("supportedMethod".equals(localName))
				supportedMethods
						.add(Integer.valueOf(attributes.getValue("id")));
			else
				expectCharacters();
		} else if ("request".equals(localName)) {
			expectCharacters();
		} else if ("error".equals(localName)) {
			expectCharacters();
		}
		// System.out.println("TellRemoteServerHandler.startElement()");
		// if ("".equals(uri))
		// System.out.println("Start element: " + qName);
		// else
		// System.out.println("Start element: {" + uri + "}" + localName);
		//
		// System.out.println(attributes);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);

		/* Potential bug, XML may not contain nested elements of <device>. */
		Log.v("tellremote", "Ending element " + localName);
		if ("device".equals(localName)) {
			inDevice = false;
			devices.add(new Device(deviceId, deviceName, deviceLastCommand,
					supportedMethods));
			supportedMethods = null;
			Log.v("tellremote", "Adding device...");
		} else if (inDevice) {
			if ("name".equals(localName))
				deviceName = pullCharacters();
			else if ("protocol".equals(localName))
				deviceProtocol = pullCharacters();
			else if ("model".equals(localName))
				deviceModel = pullCharacters();
			else if ("lastcmd".equals(localName)) {
				String lastCmd = pullCharacters();
				if ("on".equalsIgnoreCase(lastCmd))
					deviceLastCommand = DeviceCommand.ON;
				else if ("off".equalsIgnoreCase(lastCmd))
					deviceLastCommand = DeviceCommand.OFF;
				else
					throw new SAXException(
							"lastcmd element contains illegal value");
			} else if (!"supportedMethod".equals(localName)) {
				deviceParameters.put(localName, pullCharacters());
			}
		} else if ("request".equals(localName)) {
			request = pullCharacters();
		} else if ("error".equals(localName)) {
			error = pullCharacters();
		}

	}

	private void expectCharacters() {
		characters = new StringBuilder();
		readCharacters = true;
	}

	private String pullCharacters() {
		String chars = new String(characters).trim();
		characters = null;
		readCharacters = false;
		return chars;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public String getRequestOnError() {
		return request;
	}

	public String getError() {
		return error;
	}
}
