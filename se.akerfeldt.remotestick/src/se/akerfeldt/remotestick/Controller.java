package se.akerfeldt.remotestick;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class Controller {

	private String name;
	private String uri;
	private String username;
	private String password;
	private List<Device> devices;
	private BasicCredentialsProvider credsProvider;
	private HttpRequestInterceptor preemptiveAuth;
	private final long id;
	private boolean refreshed;
	
	public Controller(int id, String name, String uri, String username,
			String password) {
		this.id = id;
		this.name = name;
		this.uri = uri;
		this.username = username;
		this.password = password;
		this.devices = new ArrayList<Device>();
		refreshed = false;
		Log.v("tellremote", "Creating Controller - " + name + ", " + uri);

		// TODO: Could possibly reuse the same Interceptor in every Controller
		this.preemptiveAuth = new HttpRequestInterceptor() {
			public void process(final HttpRequest request,
					final HttpContext context) throws HttpException,
					IOException {
				AuthState authState = (AuthState) context
						.getAttribute(ClientContext.TARGET_AUTH_STATE);
				CredentialsProvider credsProvider = (CredentialsProvider) context
						.getAttribute(ClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost) context
						.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

				if (authState.getAuthScheme() == null) {
					AuthScope authScope = new AuthScope(targetHost
							.getHostName(), targetHost.getPort());
					Credentials creds = credsProvider.getCredentials(authScope);
					if (creds != null) {
						authState.setAuthScheme(new BasicScheme());
						authState.setCredentials(creds);
					}
				}
			}
		};

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public long getId() {
		return id;
	}

	public Device createDevice(String name, String model, String protocol,
			Map<String, String> parameters) {

		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			DefaultHttpClient client = new DefaultHttpClient();
			client.addRequestInterceptor(preemptiveAuth, 0);
			client.setCredentialsProvider(getCredentialsProvider());
			String url = uri + "/devices.xml";
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

	public Response refresh() {
		refreshed = true;
		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			DefaultHttpClient client = new DefaultHttpClient();
			client.addRequestInterceptor(preemptiveAuth, 0);
			client.setCredentialsProvider(getCredentialsProvider());
			URI resource = new URI(uri + "/devices.xml");
			HttpGet request = new HttpGet(resource);
			HttpResponse response = client.execute(request);
			Log.v("tellremote", response.getStatusLine().toString());
			parser.parse(response.getEntity().getContent(), handler);
			devices.clear();
			if (response.getStatusLine().getStatusCode() == 200) {
				devices.addAll(handler.getDevices());
				for (Device device : devices) {
					Log.v("tellremote", device.toString());
				}
				return new Response(true, 200);
			} else if (response.getStatusLine().getStatusCode() != 404) {
				Log.v("tellremote", handler.getError() + " on request "
						+ handler.getRequestOnError());
				return new Response(false, response.getStatusLine().getStatusCode(), handler.getError());
			} else {
				Log.v("tellremote", "404 Error");
				return new Response(false, 404, "Error while refreshing devices: Resource not found");
			}
		} catch (Exception e) {
			Log.v("tellremote", "Exception " + e.getMessage());
			e.printStackTrace();
			return new Response(false, 1, "Error: " + e.getMessage());
		}
	}

	public Response dim(Device device, int level) {
		return invokeMethod(device, "dim/" + level + ".xml");
	}
	
	public Response turnOn(Device device) {
		return invokeMethod(device, "on.xml");
	}

	public Response turnOff(Device device) {
		return invokeMethod(device, "off.xml");
	}

	private Response invokeMethod(Device device, String method) {
		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			DefaultHttpClient client = new DefaultHttpClient();
			client.addRequestInterceptor(preemptiveAuth, 0);
			client.setCredentialsProvider(getCredentialsProvider());
			String url = uri + "/devices/" + device.getId() + "/" + method;
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200)
				return new Response(true, 200);
			else if (response.getStatusLine().getStatusCode() != 404) {
				parser.parse(response.getEntity().getContent(), handler);
				Log.v("tellremote", handler.getError() + " on request "
						+ handler.getRequestOnError());
				return new Response(false, response.getStatusLine().getStatusCode(), handler.getError());
			} else
				return new Response(false, 404, "Error: Resource not found");
		} catch (Exception e) {
			Log.v("tellremote", "Exception " + e.getMessage());
			e.printStackTrace();
			return new Response(false, 1, "Error: " + e.getMessage());
		}
	}

	private CredentialsProvider getCredentialsProvider()
			throws URISyntaxException {
		if (this.credsProvider == null) {
			URI uri = new URI(this.uri);
			credsProvider = new BasicCredentialsProvider();
			Credentials credentials = new UsernamePasswordCredentials(username,
					password);
			AuthScope authscope = new AuthScope(uri.getHost(), uri.getPort());
			credsProvider.setCredentials(authscope, credentials);
		}
		return credsProvider;

	}

	public boolean delete(Device device) {
		TellRemoteServerHandler handler = new TellRemoteServerHandler();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			DefaultHttpClient client = new DefaultHttpClient();
			client.addRequestInterceptor(preemptiveAuth, 0);
			client.setCredentialsProvider(getCredentialsProvider());
			String url = uri + "/devices/" + device.getId() + ".xml";

			HttpDelete request = new HttpDelete(url);
			HttpResponse response = client.execute(request);

			if (response.getStatusLine().getStatusCode() == 200) {
				devices.remove(device);
				return true;
			} else if (response.getStatusLine().getStatusCode() != 404) {
				parser.parse(response.getEntity().getContent(), handler);
				Log.v("tellremote", handler.getError() + " on request "
						+ handler.getRequestOnError());
				return false;
			} else
				return false;
		} catch (Exception e) {
			Log.v("tellremote", "Exception " + e.getMessage());
			e.printStackTrace();
			return false;
		}

	}
	
	public void dispose() {
		devices.clear();
	}

	public boolean isRefreshed() {
		return refreshed;
	}


}
