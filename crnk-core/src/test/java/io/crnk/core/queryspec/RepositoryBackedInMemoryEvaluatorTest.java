package io.crnk.core.queryspec;

import java.util.Arrays;
import java.util.List;

import io.crnk.core.CoreTestModule;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

public class RepositoryBackedInMemoryEvaluatorTest extends InMemoryEvaluatorTestBase {


	@Override
	protected InMemoryEvaluator getEvaluator() {
		SimpleModule module = new SimpleModule("test");
		module.addRepository(new RelationIdTestRepository());

		CrnkBoot boot = new CrnkBoot();
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
}
