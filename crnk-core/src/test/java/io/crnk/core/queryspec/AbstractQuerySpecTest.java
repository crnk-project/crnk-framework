package io.crnk.core.queryspec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.contributor.ResourceFieldContributorContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import org.junit.Before;

public abstract class AbstractQuerySpecTest {


	protected ResourceRegistry resourceRegistry;

	protected ModuleRegistry moduleRegistry;

	protected CoreTestContainer container;

	protected static void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}

	@Before
	public void setup() {
		container = new CoreTestContainer();
		ResourceFieldContributor contributor = new ResourceFieldContributor() {
			@Override
			public List<ResourceField> getResourceFields(ResourceFieldContributorContext context) {
				List<ResourceField> fields = new ArrayList<>();
				if (context.getResourceInformation().getResourceClass() == Task.class) {
					// add additional field that is not defined on the class
					String name = "computedAttribute";
					ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true, true);

					InformationBuilder informationBuilder = new DefaultInformationBuilder(new TypeParser());

					InformationBuilder.FieldInformationBuilder fieldBuilder = informationBuilder.createResourceField();
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

						@Override
						public Class getImplementationClass() {
							return Integer.class;
						}
					});
					fields.add(fieldBuilder.build());

				}
				return fields;
			}
		};
		SimpleModule module = new SimpleModule("test");
		module.addResourceFieldContributor(contributor);

		setup(container);
		container.addModule(module);
		container.boot();

		moduleRegistry = container.getModuleRegistry();
		resourceRegistry = container.getResourceRegistry();
	}

	protected void setup(CoreTestContainer container) {
		container.addModule(new CoreTestModule());
	}

	protected QuerySpec querySpec(Long offset, Long limit) {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.setPagingSpec(new OffsetLimitPagingSpec(offset, limit));
		return querySpec;
	}

	protected QuerySpec querySpec() {
		return querySpec(0L, null);
	}
}
