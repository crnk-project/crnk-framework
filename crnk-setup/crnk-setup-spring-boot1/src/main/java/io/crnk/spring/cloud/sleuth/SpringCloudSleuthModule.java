package io.crnk.spring.cloud.sleuth;

import io.crnk.core.module.Module;
import io.crnk.spring.cloud.sleuth.internal.SleuthRepositoryFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;


/**
 * Traces all repository accesses. Keep in mind that a single HTTP request
 * can trigger multiple repository accesses if the request contains an inclusion of relations.
 * Note that no HTTP calls itself are traced by this module. That is the responsibility of the
 * web container/sleuth.
 */
public class SpringCloudSleuthModule implements Module {

	@Autowired
	private Tracer tracer;

	@Override
	public String getModuleName() {
		return "spring.cloud.sleuth";
	}

	@Override
	public void setupModule(ModuleContext context) {
		SleuthRepositoryFilter filter = new SleuthRepositoryFilter(tracer, context);
		context.addRepositoryFilter(filter);
	}

	public Tracer getTracer() {
		return tracer;
	}
}
