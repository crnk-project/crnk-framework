package io.crnk.test.mock.dynamic;


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

		for (int i = 0; i < 2; i++) {
			RegistryEntryBuilder builder = context.newRegistryEntryBuilder();

			String resourceType = "dynamic" + i;
			RegistryEntryBuilder.ResourceRepository resourceRepository = builder.resourceRepository();
			resourceRepository.instance(new DynamicResourceRepository(resourceType));

			RegistryEntryBuilder.RelationshipRepository relationshipRepository = builder.relationshipRepository(resourceType);
			relationshipRepository.instance(new DynamicRelationshipRepository(resourceType));

			InformationBuilder.Resource resource = builder.resource();
			resource.resourceType(resourceType);
			resource.resourceClass(Resource.class);
			resource.addField("id", ResourceFieldType.ID, String.class);
			resource.addField("value", ResourceFieldType.ATTRIBUTE, String.class);
			resource.addField("parent", ResourceFieldType.RELATIONSHIP, Resource.class).oppositeResourceType(resourceType).oppositeName("children");
			resource.addField("children", ResourceFieldType.RELATIONSHIP, List.class).oppositeResourceType(resourceType).oppositeName("parent");

			context.addRegistryEntry(builder.build());
		}
	}
}
// end::docs[]