package io.crnk.monitor.brave;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import brave.Tracing;
import brave.http.HttpTracing;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.apache.HttpClientAdapterListener;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
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
		try {
			// load classes lazily since necessary classes may not be on classpath
			if (adapter instanceof OkHttpAdapter) {
				OkHttpAdapter okHttpAdapter = (OkHttpAdapter) adapter;
				Class integrationClass = getClass().getClassLoader().loadClass(
						"io.crnk.monitor.brave.internal.OkHttpBraveIntegration"
				);
				Constructor constructor = integrationClass.getConstructor(HttpTracing.class);
				OkHttpAdapterListener listener = (OkHttpAdapterListener) constructor.newInstance(tracing);
				okHttpAdapter.addListener(listener);
			}
			else if (adapter instanceof HttpClientAdapter) {
				HttpClientAdapter httpClientAdapter = (HttpClientAdapter) adapter;

				Class integrationClass = getClass().getClassLoader().loadClass(
						"io.crnk.monitor.brave.internal.HttpClientBraveIntegration"
				);
				Constructor constructor = integrationClass.getConstructor(HttpTracing.class);
				HttpClientAdapterListener listener = (HttpClientAdapterListener) constructor.newInstance(tracing);
				httpClientAdapter.addListener(listener);
			}
			else {
				throw new IllegalArgumentException(adapter.getClass() + " not supported yet");
			}
		}
		catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException |
				ClassNotFoundException e) {
			throw new IllegalStateException("failed to setup brave integration", e);
		}
	}

	public HttpTracing getHttpTracing() {
		return tracing;
	}
}
