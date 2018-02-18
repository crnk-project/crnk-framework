package io.crnk.legacy.queryParams;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.legacy.internal.DefaultQuerySpecConverter;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;

import org.junit.Before;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractQueryParamsTest {

	protected DefaultQueryParamsConverter converter;

	protected DefaultQuerySpecConverter paramsToSpecConverter;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	protected ResourceRegistry resourceRegistry;

	protected ModuleRegistry moduleRegistry;

	protected static void addParams(Map<String, Set<String>> params, String key, String... values) {
		params.put(key, new HashSet<String>(Arrays.asList(values)));
	}

	@Before
	public void setup() {
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationProvider resourceInformationProvider =
				new DefaultResourceInformationProvider(
					new NullPropertiesProvider(),
					new DefaultResourceFieldInformationProvider(),
					new JacksonResourceFieldInformationProvider());

		SimpleModule testModule = new SimpleModule("test");

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(testModule);

		boot.boot();
		moduleRegistry = boot.getModuleRegistry();
		resourceRegistry = boot.getResourceRegistry();
		converter = new DefaultQueryParamsConverter(resourceRegistry);
		paramsToSpecConverter = new DefaultQuerySpecConverter(moduleRegistry);
	}
}