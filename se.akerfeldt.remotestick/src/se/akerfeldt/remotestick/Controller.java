package se.akerfeldt.remotestick;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class Controller {

	private String name = "MyName";
	private String uri = "MyUri";
	private String apiKey = "MyApiKey";
	private List<Device> devices;

	public Controller(String name, String uri, String apiKey) {
		this.name = name;
		this.uri = uri;
		this.apiKey = apiKey;
		this.devices = new ArrayList<Device>();
		Log.v("tellremote", "Creating Controller - " + name + ", " + uri + ", " + apiKey);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public Device createDevice(String name2, String model, String protocol,
			Map<String, String> parameters) {

		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			HttpClient client = new DefaultHttpClient();
			String url = uri + "/devices/?apikey=" + apiKey;
			HttpPost request = new HttpPost(url);
			List<NameValuePair> reqData = new ArrayList<NameValuePair>(2);
			reqData.add(new BasicNameValuePair("name", name));
			reqData.add(new BasicNameValuePair("model", model));
			reqData.add(new BasicNameValuePair("protocol", protocol));
			StringBuffer params = new StringBuffer();
			boolean first = true;
			for (Entry<String, String> entry : parameters.entrySet()) {
				if (!first)
					params.append(" ");
				else
					first = false;
				params.append(entry.getKey() + "=" + entry.getValue());
			}
			reqData
					.add(new BasicNameValuePair("parameters", params.toString()));

			request.setEntity(new UrlEncodedFormEntity(reqData));
			HttpResponse response = client.execute(request);
			parser.parse(response.getEntity().getContent(), handler);
			if (response.getStatusLine().getStatusCode() == 200)
				if (handler.getDevices().size() != 1)
					return null;
				else {
					Device device = handler.getDevices().get(0);
					devices.add(device);
					return device;
				}
			else
				return null;
		} catch (Exception e) {
			Log.v("tellremote", "Exception " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public boolean refresh() {
		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			HttpClient client = new DefaultHttpClient();
			String url = uri + "/devices/?apikey=" + apiKey;
			Log.v("tellremote", "Querying: " + url);
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			parser.parse(response.getEntity().getContent(), handler);
			devices.clear();
			if (response.getStatusLine().getStatusCode() == 200) {
				devices.addAll(handler.getDevices());
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.v("tellremote", "Exception " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public boolean turnOn(Device device) {
		return invokeMethod(device, "on");
	}
	
	public boolean turnOff(Device device) {
		return invokeMethod(device, "off");
	}
	
	private boolean invokeMethod(Device device, String method) {
		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			HttpClient client = new DefaultHttpClient();
			String url = uri + "/devices/" + device.getId()
					+ "/" + method
					+ "?apikey=" + apiKey;
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			parser.parse(response.getEntity().getContent(), handler);
			if (response.getStatusLine().getStatusCode() == 200)
				return true;
			else
				return false;
		} catch (Exception e) {
			Log.v("tellremote", "Exception " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}


}
