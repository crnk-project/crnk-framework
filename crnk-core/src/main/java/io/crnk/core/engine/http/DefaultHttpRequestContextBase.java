package io.crnk.core.engine.http;

import java.net.URI;

public abstract class DefaultHttpRequestContextBase implements HttpRequestContextBase {

	private boolean readForwardedHeader = true;

	public boolean getReadForwardedHeader() {
		return readForwardedHeader;
	}

	public void setReadForwardedHeader(boolean readForwardedHeader) {
		this.readForwardedHeader = readForwardedHeader;
	}

	@Override
	public final URI getRequestUri() {
		URI requestUri = getNativeRequestUri();
		if (readForwardedHeader) {
			String forwardedProto = this.getRequestHeader(HttpHeaders.X_FORWARDED_PROTO_HEADER);
			String scheme = requestUri.getScheme();
			if (forwardedProto != null && scheme != null && scheme.equals("http")) {
				requestUri = URI.create(forwardedProto + "://" + requestUri.toString().substring(7));
			}
		}
		return requestUri;
	}

	protected abstract URI getNativeRequestUri();

}