package io.crnk.core.module;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInstanceBuilder;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;

import java.util.Arrays;
import java.util.List;

public class TestResourceInformationProvider implements ResourceInformationProvider {

	private ResourceInformationProviderContext context;

	@Override
	public boolean accept(Class<?> resourceClass) {
		return resourceClass == TestResource.class;
	}

	@Override
	public ResourceInformation build(Class<?> resourceClass) {
		ResourceField idField = new ResourceFieldImpl("testId", "id", ResourceFieldType.ID, Integer.class, Integer.class, null);
		List<ResourceField> fields = Arrays.asList(idField);
		TypeParser typeParser = context.getTypeParser();
		ResourceInformation info = new ResourceInformation(typeParser, resourceClass, resourceClass.getSimpleName(), null,
				fields,
				OffsetLimitPagingSpec.class);
		return info;
	}

	public ResourceInformation buildWithoutResourcePath(Class<?> resourceClass) {
		ResourceField idField = new ResourceFieldImpl("testId", "id", ResourceFieldType.ID, Integer.class, Integer.class, null);
		List<ResourceField> fields = Arrays.asList(idField);
		TypeParser typeParser = context.getTypeParser();
		DefaultResourceInstanceBuilder<?> instanceBuilder = new DefaultResourceInstanceBuilder(resourceClass);
		ResourceInformation info =
				new ResourceInformation(typeParser, resourceClass, resourceClass.getSimpleName(), null, instanceBuilder, fields,
						OffsetLimitPagingSpec.class);
		return info;
	}


	@Override
	public String getResourceType(Class<?> clazz) {
		return "testId";
	}

	@Override
	public String getResourcePath(Class<?> clazz) {
		return getResourceType(clazz);
	}

	@Override
	public void init(ResourceInformationProviderContext context) {
		this.context = context;
	}

}