package io.crnk.core.module;

import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.parser.TypeParser;

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
		ResourceField idField = new ResourceFieldImpl("testId", "id", ResourceFieldType.ID, Integer.class, null, null);
		List<ResourceField> fields = Arrays.asList(idField);
		TypeParser typeParser = context.getTypeParser();
		ResourceInformation info = new ResourceInformation(typeParser, resourceClass, resourceClass.getSimpleName(), null, fields);
		return info;
	}

	@Override
	public String getResourceType(Class<?> clazz) {
		return "testId";
	}

	@Override
	public void init(ResourceInformationProviderContext context) {
		this.context = context;
	}

}