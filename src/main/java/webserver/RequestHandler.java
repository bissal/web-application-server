package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	private RequestParser parser;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
		parser = new RequestParser();
	}

	public void run() {
		log.debug("####################################################");
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			Request request = parser.parseRequest(in);
			
			String path = request.getPath();
			
			if (path.equals("/")) {
				byte[] body = "Hello World".getBytes();
				
				sendOKResponse(body, out);
			}
			
			File requested = new File("./webapp" + path);
			log.debug("requested file: " + requested);
			if (requested.exists()) {
				byte[] body = Files.readAllBytes(requested.toPath());
				sendOKResponse(body, out);
			}
			
			if (path.equals("/user/create")) {				
				addUser(request);
				send302FoundResponse(out);
			}
			
			if (path.equals("/user/login")) {				
				login(out, request);
			}
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void login(OutputStream out, Request request) {
		if (authenticate(request)) {
			sendLoginResultResponse(out, true);
		}
//		send302FoundResponse(out);
	}

	private boolean authenticate(Request request) {
		Map<String, String> userInfo = null;
		String requestMethod = request.getMethod();
		
		if (requestMethod.equals("POST")) {
			userInfo = HttpRequestUtils.parseQueryString(request.getBody());
		}
		
		if (userInfo == null) {
			return false;
		}
		
		User findUserById = DataBase.findUserById(userInfo.get("userId"));
		if (findUserById == null) {
			return false;
		}
		return findUserById.getPassword().equals(userInfo.get("password"));
	}
	
	private void addUser(Request request) {
		Map<String, String> userInfo = null;
		String requestMethod = request.getMethod();
		
		if (requestMethod.equals("GET")) {
			userInfo = HttpRequestUtils.parseQueryString(request.getQueryString());
		}
		
		if (requestMethod.equals("POST")) {
			userInfo = HttpRequestUtils.parseQueryString(request.getBody());
		}
		
		User user = new User(userInfo.get("userId"), userInfo.get("password"), userInfo.get("name"), userInfo.get("email"));
		DataBase.addUser(user);
		User findUserById = DataBase.findUserById(userInfo.get("userId"));
		log.debug("Added User: " + findUserById);
	}
	
	private void send302FoundResponse(OutputStream out) {
		DataOutputStream dos = new DataOutputStream(out);
		response302Header(dos);
	}

	private void sendLoginResultResponse(OutputStream out, boolean result) {
		DataOutputStream dos = new DataOutputStream(out);
		response200HeaderWithLoginResult(dos, result);
	}
	
	private void response200HeaderWithLoginResult(DataOutputStream dos, boolean result) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Set-Cookie: logined=" + result + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void sendOKResponse(byte[] body, OutputStream out) {
		DataOutputStream dos = new DataOutputStream(out);
		response200Header(dos, body.length);
		responseBody(dos, body);
	}

	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			dos.writeBytes("Location: /index.html\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
