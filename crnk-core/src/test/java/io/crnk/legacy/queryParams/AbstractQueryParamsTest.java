package io.crnk.legacy.queryParams;

import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.DefaultResourceLookup;
import io.crnk.legacy.internal.DefaultQuerySpecConverter;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
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
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer(), new JacksonResourceFieldInformationProvider());
		moduleRegistry = new ModuleRegistry();
		ResourceRegistryBuilder resourceRegistryBuilder = new ResourceRegistryBuilder(moduleRegistry, jsonServiceLocator, resourceInformationBuilder);
		DefaultResourceLookup resourceLookup = newResourceLookup();
		resourceRegistry = resourceRegistryBuilder.build(resourceLookup, moduleRegistry, new ConstantServiceUrlProvider("http://127.0.0.1"));
		moduleRegistry.setResourceRegistry(resourceRegistry);
		converter = new DefaultQueryParamsConverter(resourceRegistry);
		paramsToSpecConverter = new DefaultQuerySpecConverter(moduleRegistry);
	}

	protected DefaultResourceLookup newResourceLookup() {
		return new DefaultResourceLookup(Task.class.getPackage().getName() + "," + getClass().getPackage().getName()) {

			@Override
			public Set<Class<?>> getResourceRepositoryClasses() {
				Set<Class<?>> set = new HashSet<>();
				set.addAll(super.getResourceRepositoryClasses());
				set.add(ScheduleRepositoryImpl.class); // not yet recognized by reflections for some reason
				return set;
			}
		};
	}
}