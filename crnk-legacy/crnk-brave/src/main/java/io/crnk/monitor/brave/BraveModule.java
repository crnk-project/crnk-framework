package io.crnk.monitor.brave;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.SpanNameProvider;
import io.crnk.monitor.brave.internal.BraveRepositoryFilter;
import io.crnk.monitor.brave.internal.HttpClientBraveIntegration;
import io.crnk.monitor.brave.internal.OkHttpBraveIntegration;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.module.HttpAdapterAware;
import io.crnk.core.module.Module;

/**
 * Integrates Brave into crnk client and server:
 * <p>
 * <ul>
 * <li>On the client-side all requests are traced with the brave-okhttp implementation.</li>
 * <li>
 * On the server-side all the repository accesses are traced. Keep in mind that a single HTTP request
 * can trigger multiple repository accesses if the request contains an inclusion of relations.
 * Note that no HTTP calls itself are traced by this module. That is the responsibility of the
 * web container and Brave.
 * </li>
 * </ul>
 *
 * @deprecated make use of crnk-monitor-brave4
 */
@Deprecated
public class BraveModule implements Module, HttpAdapterAware {

	private Brave brave;

	private boolean server;

	private SpanNameProvider spanNameProvider;

	// protected for CDI
	protected BraveModule(){
	}

	private BraveModule(Brave brave, boolean server) {
		this.brave = brave;
		this.server = server;
		this.setSpanNameProvider(new DefaultSpanNameProvider());
	}

	public static BraveModule newClientModule(Brave brave) {
		return new BraveModule(brave, false);
	}

	public static BraveModule newServerModule(Brave brave) {
		return new BraveModule(brave, true);
	}

	public void setSpanNameProvider(SpanNameProvider spanNameProvider) {
		this.spanNameProvider = spanNameProvider;
	}

	@Override
	public String getModuleName() {
		return "brave";
	}

	@Override
	public void setupModule(ModuleContext context) {
		// nothing to do
		if (server) {
			BraveRepositoryFilter filter = new BraveRepositoryFilter(brave, context);
			context.addRepositoryFilter(filter);
		}
	}

	@Override
	public void setHttpAdapter(HttpAdapter adapter) {
		if (adapter instanceof OkHttpAdapter) {
			OkHttpAdapter okHttpAdapter = (OkHttpAdapter) adapter;
			okHttpAdapter.addListener(new OkHttpBraveIntegration(brave));
		}
		else if (adapter instanceof HttpClientAdapter) {
			HttpClientAdapter okHttpAdapter = (HttpClientAdapter) adapter;
			okHttpAdapter.addListener(new HttpClientBraveIntegration(brave, spanNameProvider));
		}
		else {
			throw new IllegalArgumentException(adapter.getClass() + " not supported yet");
		}
	}

	public Brave getBrave() {
		return brave;
	}
}
