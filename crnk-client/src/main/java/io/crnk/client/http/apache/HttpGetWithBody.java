package io.crnk.client.http.apache;

import io.crnk.core.engine.http.HttpMethod;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.net.URI;

public class HttpGetWithBody extends HttpEntityEnclosingRequestBase {
	public HttpGetWithBody(String url, String body, ContentType contentType) {
		setURI(URI.create(url));
		setEntity(new StringEntity(body, contentType));
	}

	@Override
	public String getMethod() {
		return HttpMethod.GET.toString();
	}
}
