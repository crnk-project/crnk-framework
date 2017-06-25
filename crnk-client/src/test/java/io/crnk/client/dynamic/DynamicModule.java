package io.crnk.client.dynamic;


import io.crnk.core.module.Module;

// tag::docs[]
public class DynamicModule implements Module {

	@Override
	public String getModuleName() {
		return "dynamic";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryInformationBuilder(new DynamicResourceRepositoryInformationBuilder());
		context.addRepositoryInformationBuilder(new DynamicRelationshipRepositoryInformationBuilder());
		context.addRepository(new DynamicResourceRepository());
		context.addRepository(new DynamicRelationshipRepository());
	}
}
// end::docs[]