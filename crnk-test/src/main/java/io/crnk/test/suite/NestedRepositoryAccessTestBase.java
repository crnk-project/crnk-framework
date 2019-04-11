package io.crnk.test.suite;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.nested.PostComment;
import io.crnk.test.mock.models.nested.PostCommentId;
import io.crnk.test.mock.models.nested.NestedRelatedResource;
import io.crnk.test.mock.models.nested.PostHeader;
import io.crnk.test.mock.models.nested.Post;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class NestedRepositoryAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepository<Post, String> parentRepo;

	protected ResourceRepository<PostComment, PostCommentId> manyNestedRepo;

	protected ResourceRepository<PostHeader, String> oneNestedRepo;

	protected ResourceRepository<NestedRelatedResource, String> relatedRepo;

	@Before
	public void setup() {
		testContainer.start();

		parentRepo = testContainer.getRepositoryForType(Post.class);
		manyNestedRepo = testContainer.getRepositoryForType(PostComment.class);
		oneNestedRepo = testContainer.getRepositoryForType(PostHeader.class);
		relatedRepo = testContainer.getRepositoryForType(NestedRelatedResource.class);

		NestedRelatedResource relatedResource = new NestedRelatedResource();
		relatedResource.setId("related");
		relatedRepo.create(relatedResource);

		Post parentResource = new Post();
		parentResource.setId("a");
		parentRepo.create(parentResource);
	}

	@After
	public void tearDown() {
		testContainer.stop();
	}


	@Test
	public void testMany() {
		PostCommentId id = new PostCommentId("a", "b");

		// perform create
		PostComment resource = new PostComment();
		resource.setId(id);
		resource.setValue("nested");
		resource = manyNestedRepo.create(resource);
		Assert.assertEquals(id, resource.getId());
		String selfUrl = resource.getLinks().getSelf();
		Assert.assertTrue(selfUrl, selfUrl.contains("a/comments/b"));
		Assert.assertEquals("nested", resource.getValue());

		// perform update
		resource.setValue("updated");
		resource = manyNestedRepo.save(resource);
		Assert.assertEquals("updated", resource.getValue());
		selfUrl = resource.getLinks().getSelf();
		Assert.assertTrue(selfUrl, selfUrl.contains("a/comments/b"));

		// perform find over all nested resources
		ResourceList<PostComment> list = manyNestedRepo.findAll(new QuerySpec(PostComment.class));
		Assert.assertEquals(1, list.size());
		resource = list.get(0);
		Assert.assertEquals("updated", resource.getValue());

		// perform delete
		manyNestedRepo.delete(id);
		try {
			manyNestedRepo.findOne(id, new QuerySpec(PostComment.class));
			Assert.fail("should no longer be available");
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void testOne() {
		// perform create
		PostHeader resource = new PostHeader();
		resource.setPostId("a");
		resource.setValue("nested");
		resource = oneNestedRepo.create(resource);
		Assert.assertEquals("a", resource.getPostId());
		String selfUrl = resource.getLinks().getSelf();
		Assert.assertTrue(selfUrl, selfUrl.contains("a/header"));
		Assert.assertEquals("nested", resource.getValue());

		// perform update
		resource.setValue("updated");
		resource = oneNestedRepo.save(resource);
		Assert.assertEquals("updated", resource.getValue());
		selfUrl = resource.getLinks().getSelf();
		Assert.assertTrue(selfUrl, selfUrl.contains("a/header"));

		// perform find over all nested resources
		ResourceList<PostHeader> list = oneNestedRepo.findAll(new QuerySpec(PostHeader.class));
		Assert.assertEquals(1, list.size());
		resource = list.get(0);
		Assert.assertEquals("updated", resource.getValue());

		// perform delete
		oneNestedRepo.delete("a");
		try {
			oneNestedRepo.findOne("a", new QuerySpec(PostHeader.class));
			Assert.fail("should no longer be available");
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}
}
