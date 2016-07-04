package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;
import util.IOUtils;

public class RequestParser {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	public RequestParser() {
	}
	
	public Request parseRequest(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		Request request = new Request();
		
		String requestLine = readRequestLine(br);
		request = parseRequestLine(requestLine, request);
		
		String[] readHeader = readHeader(br);
		request = parseHeader(readHeader, request);
		
		log.debug("parsed request: " + request);
		
		String sContentLength = request.getRequestHeader("Content-Length");
		if (sContentLength == null) {
			return request;
		}
		int contentLength = Integer.parseInt(sContentLength);
		if (contentLength > 0) {
			request.setBody(readBody(br, contentLength));
		}
		
		return request;
	}

	private String readBody(BufferedReader br, int contentLength) throws IOException {
		String requestBody;
		requestBody = IOUtils.readData(br, contentLength);
		log.debug("requestBody: " + requestBody);
		
		return requestBody;
	}

	private Request parseHeader(String[] readHeader, Request request) {
		for (String string : readHeader) {
			Pair parseHeader = HttpRequestUtils.parseHeader(string);
			request.addRequestHeader(parseHeader.getKey(), parseHeader.getValue());
		}
		return request;
	}

	private String[] readHeader(BufferedReader br) throws IOException {
		List<String> lines = new ArrayList<String>();
		String line = null;
		log.debug("--------------------");
		while (!"".equals(line)) {
			line = br.readLine();
			if (line == null || line.isEmpty()) {
				break;
			}
			log.debug("readline: " + line);
			
			lines.add(line);
		}
		log.debug("--------------------");
		
		return lines.toArray(new String[lines.size()]);
	}

	private Request parseRequestLine(String requestLine, Request request) {
		String[] tokens = requestLine.split(" ");
		log.debug("Arrays.toString(tokens): " + Arrays.toString(tokens));
		
		request.setMethod(tokens[0]);
		
		String requestedtUrl = tokens[1];
		log.debug("requestUrl: " + requestedtUrl);
		
		if (requestedtUrl.contains("?")) {
			int index = requestedtUrl.indexOf("?");
			String requestPath = requestedtUrl.substring(0, index);
			String params = requestedtUrl.substring(index + 1);
			
			request.setPath(requestPath);
			request.setQueryString(params);
		} else {
			request.setPath(requestedtUrl);
		}
		return request;
	}

	private String readRequestLine(BufferedReader br) throws IOException {
		return br.readLine();
	}
}