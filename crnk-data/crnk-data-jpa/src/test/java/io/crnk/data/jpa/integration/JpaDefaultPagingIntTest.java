package io.crnk.data.jpa.integration;

import io.crnk.client.response.JsonLinksInformation;
import io.crnk.client.response.JsonMetaInformation;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.core.resource.meta.PagedMetaInformation;
import io.crnk.data.jpa.AbstractJpaJerseyTest;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.rs.CrnkFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JpaDefaultPagingIntTest extends AbstractJpaJerseyTest {

	private ResourceRepository<TestEntity, Long> testRepo;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);

		for (long i = 0; i < 50; i++) {
			TestEntity task = new TestEntity();
			task.setId(i);
			task.setStringValue("test");
			testRepo.create(task);
		}
	}


	@Test
	public void testRootPaging() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<TestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		JsonMetaInformation meta = list.getMeta(JsonMetaInformation.class);
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		Assert.assertNotNull(meta);
		Assert.assertNotNull(links);

		String baseUri = getBaseUri().toString();
		Assert.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("first").asText());
		Assert.assertEquals(baseUri + "test?page[limit]=2&page[offset]=48", links.asJsonNode().get("last").asText());
		Assert.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("prev").asText());
		Assert.assertEquals(baseUri + "test?page[limit]=2&page[offset]=4", links.asJsonNode().get("next").asText());
	}

	@Test
	public void testDefaultLimitInPlace() {
		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		ResourceList<TestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(10, list.size());
		PagedMetaInformation meta = list.getMeta(DefaultPagedMetaInformation.class);
		Assert.assertEquals(50, meta.getTotalResourceCount().longValue());
	}

	@Override
	protected void setupFeature(CrnkFeature feature) {
		feature.setDefaultPageLimit(10L);
	}
}
