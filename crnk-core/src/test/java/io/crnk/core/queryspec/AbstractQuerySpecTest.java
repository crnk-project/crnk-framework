package io.crnk.core.queryspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.DefaultResourceLookup;
import io.crnk.legacy.internal.DefaultQuerySpecConverter;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
import org.junit.Before;

import java.util.*;

public abstract class AbstractQuerySpecTest {

	protected DefaultQuerySpecConverter parser;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	protected ResourceRegistry resourceRegistry;

	protected ModuleRegistry moduleRegistry;

	protected static void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<String>(Arrays.asList(value)));
	}

	@Before
	public void setup() {
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer(), new JacksonResourceFieldInformationProvider()) {

			@Override
			protected List<AnnotatedResourceField> getResourceFields(Class<?> resourceClass) {
				List<AnnotatedResourceField> fields = super.getResourceFields(resourceClass);

				if (resourceClass == Task.class) {
					// add additional field that is not defined on the class
					String name = "computedAttribute";
					ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true);
					AnnotatedResourceField field = new AnnotatedResourceField(name, name, Integer.class, Integer.class, null, (List) Collections.emptyList(), access);
					field.setAccessor(new ResourceFieldAccessor() {

						public Object getValue(Object resource) {
							return 13;
						}

						public void setValue(Object resource, Object fieldValue) {

						}
					});
					fields.add(field);
				}
				return fields;
			}
		};
		moduleRegistry = new ModuleRegistry();
		ResourceRegistryBuilder resourceRegistryBuilder = new ResourceRegistryBuilder(moduleRegistry, jsonServiceLocator, resourceInformationBuilder);
		DefaultResourceLookup resourceLookup = newResourceLookup();
		ServiceUrlProvider serviceUrlProvider = new ConstantServiceUrlProvider("http://127.0.0.1");
		moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(serviceUrlProvider);
		resourceRegistry = resourceRegistryBuilder.build(resourceLookup, moduleRegistry, serviceUrlProvider);
		moduleRegistry.setResourceRegistry(resourceRegistry);
		moduleRegistry.init(new ObjectMapper());
		parser = new DefaultQuerySpecConverter(moduleRegistry);
	}

	protected DefaultResourceLookup newResourceLookup() {
		return new DefaultResourceLookup(Task.class.getPackage().getName() + "," + getClass().getPackage().getName()) {

			@Override
			public Set<Class<?>> getResourceRepositoryClasses() {
				Set<Class<?>> set = new HashSet<>();
				set.addAll(super.getResourceRepositoryClasses());
				set.add(ScheduleRepositoryImpl.class); // not yet recognized by
				// reflections for some
				// reason
				return set;
			}
		};
	}
}
