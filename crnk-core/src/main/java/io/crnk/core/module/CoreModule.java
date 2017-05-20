package io.crnk.core.module;

import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.internal.exception.DefaultExceptionMapperLookup;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.module.discovery.DefaultResourceLookup;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationBuilder;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationBuilder;

/**
 * Register the Crnk core feature set as module.
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
		this.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(resourceFieldNameTransformer));
		this.addRepositoryInformationBuilder(new DefaultResourceRepositoryInformationBuilder());
		this.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationBuilder());
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.addHttpRequestProcessor(new JsonApiRequestProcessor(context));
		super.setupModule(context);
	}
}
