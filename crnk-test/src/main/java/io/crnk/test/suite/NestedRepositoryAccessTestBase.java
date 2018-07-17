package io.crnk.test.suite;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.nested.NestedId;
import io.crnk.test.mock.models.nested.ManyNestedResource;
import io.crnk.test.mock.models.nested.ParentResource;
import io.crnk.test.mock.models.nested.NestedRelatedResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class NestedRepositoryAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepositoryV2<ParentResource, String> parentRepo;

	protected ResourceRepositoryV2<ManyNestedResource, NestedId> nestedRepo;

	protected ResourceRepositoryV2<NestedRelatedResource, String> relatedRepo;

	@Before
	public void setup() {
		testContainer.start();

		parentRepo = testContainer.getRepositoryForType(ParentResource.class);
		nestedRepo = testContainer.getRepositoryForType(ManyNestedResource.class);
		relatedRepo = testContainer.getRepositoryForType(NestedRelatedResource.class);

		NestedRelatedResource relatedResource = new NestedRelatedResource();
		relatedResource.setId("related");
		relatedRepo.create(relatedResource);

		ParentResource parentResource = new ParentResource();
		parentResource.setId("a");
		parentRepo.create(parentResource);
	}

	@After
	public void tearDown() {
		testContainer.stop();
	}


	@Test
	public void test() {
		NestedId id = new NestedId("a", "b");

		// perform create
		ManyNestedResource resource = new ManyNestedResource();
		resource.setId(id);
		resource.setValue("nested");
		resource = nestedRepo.create(resource);
		Assert.assertEquals(id, resource.getId());
		String selfUrl = resource.getLinks().getSelf();
		Assert.assertTrue(selfUrl, selfUrl.contains("a/manyNested/b"));
		Assert.assertEquals("nested", resource.getValue());

		// perform update
		resource.setValue("updated");
		resource = nestedRepo.save(resource);
		Assert.assertEquals("updated", resource.getValue());
		selfUrl = resource.getLinks().getSelf();
		Assert.assertTrue(selfUrl, selfUrl.contains("a/manyNested/b"));

		// perform find over all nested resources
		ResourceList<ManyNestedResource> list = nestedRepo.findAll(new QuerySpec(ManyNestedResource.class));
		Assert.assertEquals(1, list.size());
		resource = list.get(0);
		Assert.assertEquals("updated", resource.getValue());

		// perform delete
		nestedRepo.delete(id);
		try {
			nestedRepo.findOne(id, new QuerySpec(ManyNestedResource.class));
			Assert.fail("should no longer be available");
		} catch (ResourceNotFoundException e) {
			// ok
		}
	}
}
