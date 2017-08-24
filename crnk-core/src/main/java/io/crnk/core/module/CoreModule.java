package io.crnk.core.module;

import io.crnk.core.engine.information.resource.AttributeSerializationInformationProvider;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JacksonAttributeSerializationInformationProvider;
import io.crnk.core.module.discovery.DefaultResourceLookup;
import io.crnk.legacy.internal.DefaultExceptionMapperLookup;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationBuilder;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationBuilder;

/**
 * Register the Crnk core feature set as module.
 *
 * @deprecated obsoleted by CrnkBoot
 */
@Deprecated
public class CoreModule extends SimpleModule {

	public static final String MODULE_NAME = "core";

	public CoreModule(String resourceSearchPackage, ResourceFieldNameTransformer resourceFieldNameTransformer) {
		this(resourceFieldNameTransformer);
		this.addResourceLookup(new DefaultResourceLookup(resourceSearchPackage));
		this.addExceptionMapperLookup(new DefaultExceptionMapperLookup(resourceSearchPackage));
	}

	public CoreModule(ResourceFieldNameTransformer resourceFieldNameTransformer) {
		super(MODULE_NAME);
		AttributeSerializationInformationProvider infoProvider = new JacksonAttributeSerializationInformationProvider();
		this.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(infoProvider, resourceFieldNameTransformer));
		this.addRepositoryInformationBuilder(new DefaultResourceRepositoryInformationBuilder());
		this.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationBuilder());
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.addHttpRequestProcessor(new JsonApiRequestProcessor(context));
		super.setupModule(context);
	}
}
