package io.crnk.brave.internal;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.http.SpanNameProvider;
import com.github.kristofa.brave.httpclient.BraveHttpRequestInterceptor;
import com.github.kristofa.brave.httpclient.BraveHttpResponseInterceptor;
import io.crnk.client.http.apache.HttpClientAdapterListener;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientBraveIntegration implements HttpClientAdapterListener {

	private Brave brave;

	private SpanNameProvider spanNameProvider;

	public HttpClientBraveIntegration(Brave brave, SpanNameProvider spanNameProvider) {
		this.brave = brave;
		this.spanNameProvider = spanNameProvider;
	}

	@Override
	public void onBuild(HttpClientBuilder builder) {
		ClientRequestInterceptor clientRequestInterceptor = brave.clientRequestInterceptor();
		ClientResponseInterceptor clientResponseInterceptor = brave.clientResponseInterceptor();
		builder.addInterceptorFirst(new BraveHttpRequestInterceptor(clientRequestInterceptor, spanNameProvider));
		builder.addInterceptorFirst(new BraveHttpResponseInterceptor(clientResponseInterceptor));
	}

}
