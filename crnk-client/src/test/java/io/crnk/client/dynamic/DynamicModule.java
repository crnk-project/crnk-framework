package io.crnk.client.dynamic;


import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.module.Module;

import java.util.List;

// tag::docs[]
public class DynamicModule implements Module {

	@Override
	public String getModuleName() {
		return "dynamic";
	}

	@Override
	public void setupModule(ModuleContext context) {
		RegistryEntryBuilder builder = context.newRegistryEntryBuilder();

		RegistryEntryBuilder.ResourceRepository resourceRepository = builder.resourceRepository();
		resourceRepository.instance(new DynamicResourceRepository());

		RegistryEntryBuilder.RelationshipRepository relationshipRepository = builder.relationshipRepository("dynamic");
		relationshipRepository.instance(new DynamicRelationshipRepository());

		InformationBuilder.Resource resource = builder.resource();
		resource.resourceType("dynamic");
		resource.resourceClass(Resource.class);
		resource.addField("id", ResourceFieldType.ID, String.class);
		resource.addField("value", ResourceFieldType.ATTRIBUTE, String.class);
		resource.addField("parent", ResourceFieldType.RELATIONSHIP, Resource.class).oppositeResourceType("dynamic");
		resource.addField("children", ResourceFieldType.RELATIONSHIP, List.class).oppositeResourceType("dynamic");

		context.addRegistryEntry(builder.build());
	}
}
// end::docs[]