package se.akerfeldt.remotestick;

public class Response {

	final private boolean ok;

	final private int responseCode;
	
	final private String errorMsg;

	public Response(boolean ok, int responseCode, String errorMsg) {
		this.ok = ok;
		this.responseCode = responseCode;
		this.errorMsg = errorMsg;
	}
	
	public Response(boolean ok, int responseCode) {
		this.ok = ok;
		this.responseCode = responseCode;
		this.errorMsg = "";
	}
	
	public boolean isOk() {
		return ok;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}
		
}
