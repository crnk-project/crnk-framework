package io.crnk.client.module;

import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonAttributeSerializationInformationProvider;
import io.crnk.core.module.Module;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationBuilder;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationBuilder;

public class ClientModule implements Module {

	@Override
	public String getModuleName() {
		return "client";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(new JacksonAttributeSerializationInformationProvider(), new ResourceFieldNameTransformer()));
		context.addRepositoryInformationBuilder(new DefaultResourceRepositoryInformationBuilder());
		context.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationBuilder());
	}

}
