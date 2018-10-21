package io.crnk.core.queryspec;

import io.crnk.core.CoreTestModule;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RepositoryBackedInMemoryEvaluatorTest extends InMemoryEvaluatorTestBase {


	private CrnkBoot boot;

	@Override
	protected InMemoryEvaluator getEvaluator() {
		SimpleModule module = new SimpleModule("test");
		module.addRepository(new RelationIdTestRepository());
		module.addRepository(new InMemoryResourceRepository(RenamedIdResource.class));

		boot = new CrnkBoot();
		boot.setPropertiesProvider(key -> {
			if (key.equals(CrnkProperties.ENFORCE_ID_NAME)) {
				return "true";
			}
			return null;
		});
		boot.addModule(new CoreTestModule());
		boot.addModule(module);
		boot.boot();

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		return new InMemoryEvaluator(resourceRegistry);
	}

	@Test
	public void testRelationId() {
		RelationIdTestResource resource = new RelationIdTestResource();
		resource.setTestRenamedDifferent(12L);

		List<RelationIdTestResource> resources = Arrays.asList(resource);

		QuerySpec matchQuerySpec = new QuerySpec(RelationIdTestResource.class);
		matchQuerySpec.addFilter(new FilterSpec(PathSpec.of("testRenamed.id"), FilterOperator.EQ, 12L));

		QuerySpec mismatchQuerySpec = new QuerySpec(RelationIdTestResource.class);
		mismatchQuerySpec.addFilter(new FilterSpec(PathSpec.of("testRenamed.id"), FilterOperator.EQ, 99999L));

		Assert.assertEquals(1, evaluator.eval(resources, matchQuerySpec).size());
		Assert.assertEquals(0, evaluator.eval(resources, mismatchQuerySpec).size());
	}

	@Test
	public void filterByRenamedIdWithIdEnforcement() {
		RegistryEntry entry = boot.getResourceRegistry().getEntry(RenamedIdResource.class);
		ResourceField idField = entry.getResourceInformation().getIdField();
		Assert.assertEquals("id", idField.getJsonName());

		RenamedIdResource resource = new RenamedIdResource();
		resource.setNotId("doe");
		List<RenamedIdResource> resources = Arrays.asList(resource);

		QuerySpec matchQuerySpec = new QuerySpec(RenamedIdResource.class);
		matchQuerySpec.addFilter(new FilterSpec(PathSpec.of("id"), FilterOperator.EQ, "doe"));

		QuerySpec mismatchQuerySpec = new QuerySpec(RelationIdTestResource.class);
		mismatchQuerySpec.addFilter(new FilterSpec(PathSpec.of("id"), FilterOperator.EQ, "other"));

		Assert.assertEquals(1, evaluator.eval(resources, matchQuerySpec).size());
		Assert.assertEquals(0, evaluator.eval(resources, mismatchQuerySpec).size());
	}

	@JsonApiResource(type = "renamedId")
	public static class RenamedIdResource {

		@JsonApiId
		private String notId;

		public String getNotId() {
			return notId;
		}

		public void setNotId(String notId) {
			this.notId = notId;
		}
	}
}
