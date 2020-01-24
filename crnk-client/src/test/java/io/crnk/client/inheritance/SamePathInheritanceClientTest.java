package io.crnk.client.inheritance;

import io.crnk.client.AbstractClientTest;
import io.crnk.client.inheritance.repositories.cyclic.CyclicResourceARepository;
import io.crnk.client.inheritance.repositories.cyclic.CyclicResourceBRespository;
import io.crnk.client.inheritance.repositories.cyclic.CyclicResourceCRepository;
import io.crnk.client.inheritance.repositories.related.RelatedResourceARepository;
import io.crnk.client.inheritance.repositories.related.RelatedResourceBRepository;
import io.crnk.client.inheritance.resources.cyclic.CyclicResourceA;
import io.crnk.client.inheritance.resources.related.RelatedResourceA;
import io.crnk.client.inheritance.resources.related.RelatedResourceAsub1;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.Assert;
import org.junit.Test;

/**
 * Examples makes use of Inheritance of resources that share a common resource path. Make sure it correctly handles
 * multiple resources with same path and able to find fields on subtypes.
 *
 * @author syri.
 */
public class SamePathInheritanceClientTest extends AbstractClientTest {


	@Override
	protected TestApplication configure() {
		TestApplication app = super.configure();
		SimpleModule cyclicModule = new SimpleModule("crnk-test");
		cyclicModule.addRepository(new CyclicResourceARepository());
		cyclicModule.addRepository(new CyclicResourceBRespository());
		cyclicModule.addRepository(new CyclicResourceCRepository());
		cyclicModule.addRepository(new RelatedResourceARepository());
		cyclicModule.addRepository(new RelatedResourceBRepository());
		app.getFeature().addModule(cyclicModule);
		return app;
	}

	@Test
	public void testInheritedRelation() {
		QuerySpec querySpec = new QuerySpec(RelatedResourceA.class);
		RelatedResourceA resource = client.getRepositoryForType(RelatedResourceA.class).findOne(1L, querySpec);
		RelatedResourceAsub1 subResource = (RelatedResourceAsub1) resource;
		Assert.assertEquals(1, subResource.getRelatedResourceBS().size());
	}

	@Test
	public void testCyclicRelationsRepository() {
		// Crash
		client.getRepositoryForType(CyclicResourceA.class);
	}

}
