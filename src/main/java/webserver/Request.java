package webserver;

import java.util.HashMap;
import java.util.Map;

public class Request {
	private String method;
	private String path;
	private String queryString;
	private Map<String, String> requestHeaderMap;
	private String body;

	@Override
	public String toString() {
		return "Request [method=" + method + ", path=" + path + ", queryString=" + queryString + ", requestHeaderMap="
				+ requestHeaderMap + ", body=" + body + "]";
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String addRequestHeader(String key, String value) {
		return getRequestHeaderMap().put(key, value);
	}
	
	public String getRequestHeader(String key) {
		return getRequestHeaderMap().get(key);
	}

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	private Map<String, String> getRequestHeaderMap() {
		if (requestHeaderMap == null) {
			requestHeaderMap = new HashMap<String, String>();
		}
		
		return requestHeaderMap;
	}
}
