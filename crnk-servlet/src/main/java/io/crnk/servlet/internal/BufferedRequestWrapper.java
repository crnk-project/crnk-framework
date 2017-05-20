package io.crnk.servlet.internal;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * A class which provides a buffered payload. Inspired by
 * <a href="http://massimilianosciacco.com/implementing-hmac-authentication-rest-api-spring-security">Implementing HMAC
 * authentication for REST API with Spring Security</a>
 */
public class BufferedRequestWrapper extends HttpServletRequestWrapper {

	private String bufferedRequest;

	public BufferedRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		this.bufferedRequest = getInputString(request);
	}

	private String getInputString(HttpServletRequest request) throws IOException {
		if (request.getInputStream() == null) {
			return null;
		}

		Scanner s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
		String requestBody = s.hasNext() ? s.next() : "";

		if (requestBody == null || requestBody.isEmpty()) {
			return "";
		}
		return requestBody;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (bufferedRequest == null) {
			return null;
		}
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bufferedRequest.getBytes());
		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
		};
	}
}
