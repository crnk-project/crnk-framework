package io.crnk.brave;

import brave.Tracing;
import brave.http.HttpTracing;
import io.crnk.brave.internal.HttpClientBraveIntegration;
import io.crnk.brave.internal.OkHttpBraveIntegration;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.module.HttpAdapterAware;
import io.crnk.core.module.Module;


/**
 * Traces all crnk-client requests with brave-instrumentation-okhttp3 or brave-instrumentation-httpclient implementation
 */
public class BraveClientModule implements Module, HttpAdapterAware {

	private HttpTracing tracing;


	// protected for CDI
	protected BraveClientModule() {
	}

	private BraveClientModule(HttpTracing tracing) {
		this.tracing = tracing;
	}

	public static BraveClientModule create(Tracing tracing) {
		return create(HttpTracing.create(tracing));
	}

	public static BraveClientModule create(HttpTracing tracing) {
		return new BraveClientModule(tracing);
	}

	@Override
	public String getModuleName() {
		return "brave-client";
	}

	@Override
	public void setupModule(ModuleContext context) {
		// nothing to do
	}

	@Override
	public void setHttpAdapter(HttpAdapter adapter) {
		if (adapter instanceof OkHttpAdapter) {
			OkHttpAdapter okHttpAdapter = (OkHttpAdapter) adapter;
			okHttpAdapter.addListener(new OkHttpBraveIntegration(tracing));
		}
		else if (adapter instanceof HttpClientAdapter) {
			HttpClientAdapter okHttpAdapter = (HttpClientAdapter) adapter;
			okHttpAdapter.addListener(new HttpClientBraveIntegration(tracing));
		}
		else {
			throw new IllegalArgumentException(adapter.getClass() + " not supported yet");
		}
	}

	public HttpTracing getHttpTracing() {
		return tracing;
	}
}
