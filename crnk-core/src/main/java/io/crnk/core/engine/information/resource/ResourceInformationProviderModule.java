package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.module.SimpleModule;

/**
 * Registers a {@link DefaultResourceInformationProvider} with the context.
 * Make sure to add the module when all paging behaviors have been registered.
 */
public class ResourceInformationProviderModule extends SimpleModule {
	public static final String NAME = "resourceInformationProviderModule";

	public ResourceInformationProviderModule() {
		super(NAME);
	}

	@Override
	public void setupModule(ModuleContext context) {
		DefaultResourceFieldInformationProvider defaultFieldProvider = new DefaultResourceFieldInformationProvider();
		ResourceFieldInformationProvider jacksonFieldProvider = new JacksonResourceFieldInformationProvider();

		context.addResourceInformationProvider(
				new DefaultResourceInformationProvider(
						context.getPropertiesProvider(),
						context.getModuleRegistry().getPagingBehaviors(),
						defaultFieldProvider,
						jacksonFieldProvider));

		super.setupModule(context);
	}
}
