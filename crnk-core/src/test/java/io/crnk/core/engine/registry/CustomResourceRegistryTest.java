package io.crnk.core.engine.registry;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.TestServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.repository.response.JsonApiResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CustomResourceRegistryTest {

	@Test
	public void test() {
		CoreTestContainer container = new CoreTestContainer();
		container.addModule(new CustomRegistryPartModule());
		container.getBoot().setServiceDiscovery(new TestServiceDiscovery());
		container.boot();

		RegistryEntry entry = container.getEntry("somePrefix/custom");
		Assert.assertNotNull(entry);
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();

		QueryAdapter queryAdapter = container.toQueryAdapter(new QuerySpec("somePrefix/custom"));

		JsonApiResponse response = adapter.findAll(queryAdapter).get();
		Assert.assertNotNull(response.getEntity());
		List<Resource> resources = (List<Resource>) response.getEntity();
		Assert.assertEquals(1, resources.size());
	}

	// tag::docs[]
	class CustomRegistryPartModule implements Module {

		@Override
		public String getModuleName() {
			return "customRegistry";
		}

		@Override
		public void setupModule(ModuleContext context) {
			RegistryEntryBuilder builder = context.newRegistryEntryBuilder();
			builder.resourceRepository().instance(new DynamicResourceRepository());
			InformationBuilder.ResourceInformationBuilder resource = builder.resource();

			resource.resourceType("somePrefix/custom");
			resource.resourceClass(Resource.class);
			resource.addField("id", ResourceFieldType.ID, String.class);
			resource.addField("value", ResourceFieldType.ATTRIBUTE, String.class);
			resource.addField("parent", ResourceFieldType.RELATIONSHIP, Resource.class).oppositeResourceType("dynamic");
			resource.addField("children", ResourceFieldType.RELATIONSHIP, List.class).oppositeResourceType("dynamic");
			resource.pagingBehavior(new OffsetLimitPagingBehavior());

			RegistryEntry resourceEntry = builder.build();
			context.addRegistryPart("somePrefix", new CustomRegistryPart(resourceEntry));
		}
	}

	/**
	 * Typical use cases may not inherit from DefaultResourceRegistryPart but rather implement the methods directly.
	 */
	class CustomRegistryPart extends DefaultResourceRegistryPart {

		public CustomRegistryPart(RegistryEntry resourceEntry) {
			addEntry(resourceEntry);
		}
	}
	// end::docs[]
}
