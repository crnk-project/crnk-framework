package io.crnk.servlet.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

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
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {

			}

			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
		};
	}
}
