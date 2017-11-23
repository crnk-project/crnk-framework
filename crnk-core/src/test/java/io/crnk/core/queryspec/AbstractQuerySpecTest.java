package io.crnk.core.queryspec;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.legacy.internal.DefaultQuerySpecConverter;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;

import org.junit.Before;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractQuerySpecTest {

	protected DefaultQuerySpecConverter querySpecConverter;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	protected ResourceRegistry resourceRegistry;

	protected ModuleRegistry moduleRegistry;

	protected static void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}

	@Before
	public void setup() {
		ResourceInformationProvider resourceInformationProvider = new DefaultResourceInformationProvider(new NullPropertiesProvider(), new DefaultResourceFieldInformationProvider(), new JacksonResourceFieldInformationProvider()) {

			@Override
			protected List<ResourceField> getResourceFields(Class<?> resourceClass) {
				List<ResourceField> fields = super.getResourceFields(resourceClass);

				if (resourceClass == Task.class) {
					// add additional field that is not defined on the class
					String name = "computedAttribute";
					ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true, true);

					InformationBuilder informationBuilder = new DefaultInformationBuilder(new TypeParser());

					InformationBuilder.Field fieldBuilder = informationBuilder.createResourceField();
					fieldBuilder.type(Integer.class);
					fieldBuilder.jsonName(name);
					fieldBuilder.underlyingName(name);
					fieldBuilder.access(access);
					fieldBuilder.accessor(new ResourceFieldAccessor() {

						public Object getValue(Object resource) {
							return 13;
						}

						public void setValue(Object resource, Object fieldValue) {

						}
					});
					fields.add(fieldBuilder.build());
				}
				return fields;
			}
		};

		SimpleModule testModule = new SimpleModule("test");
		testModule.addResourceInformationProvider(resourceInformationProvider);

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://127.0.0.1"));
		boot.addModule(testModule);
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(getResourceSearchPackage()));
		boot.boot();

		moduleRegistry = boot.getModuleRegistry();
		querySpecConverter = new DefaultQuerySpecConverter(moduleRegistry);
		resourceRegistry = boot.getResourceRegistry();
	}

	public String getResourceSearchPackage() {
		return getClass().getPackage().getName();
	}
}
