package io.crnk.jpa.integration;

import io.crnk.client.response.JsonLinksInformation;
import io.crnk.client.response.JsonMetaInformation;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.model.RelatedEntity;
import io.crnk.jpa.model.TestEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;

public class HasNextPagingIntTest extends AbstractJpaJerseyTest {

	private ResourceRepositoryV2<TestEntity, Long> testRepo;

	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		super.setupModule(module, server);
		if (server) {
			module.setTotalResourceCountUsed(false);
		}
	}


	@Test
	public void testRootPaging() {
		for (long i = 0; i < 5; i++) {
			TestEntity task = new TestEntity();
			task.setId(i);
			task.setStringValue("test");
			testRepo.create(task);
		}

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<TestEntity> list = testRepo.findAll(querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);

		String baseUri = getBaseUri().toString();
		Assert.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("first").asText());
		Assert.assertNull(links.asJsonNode().get("last")); // not available for hasNext
		Assert.assertEquals(baseUri + "test?page[limit]=2", links.asJsonNode().get("prev").asText());
		Assert.assertEquals(baseUri + "test?page[limit]=2&page[offset]=4", links.asJsonNode().get("next").asText());

		JsonMetaInformation meta = list.getMeta(JsonMetaInformation.class);
		Assert.assertNull(meta);
	}

	@Test
	public void testRelationPaging() {
		TestEntity test = new TestEntity();
		test.setId(1L);
		test.setStringValue("test");
		testRepo.create(test);

		ResourceRepositoryV2<RelatedEntity, Serializable> relatedRepo = client.getRepositoryForType(RelatedEntity.class);
		RelationshipRepositoryV2<TestEntity, Long, RelatedEntity, Long> relRepo = client
				.getRepositoryForType(TestEntity.class, RelatedEntity.class);

		for (long i = 0; i < 5; i++) {
			RelatedEntity related1 = new RelatedEntity();
			related1.setId(i);
			related1.setStringValue("related" + i);
			relatedRepo.create(related1);

			relRepo.addRelations(test, Arrays.asList(i), TestEntity.ATTR_manyRelatedValues);
		}

		QuerySpec querySpec = new QuerySpec(RelatedEntity.class);
		querySpec.setOffset(2L);
		querySpec.setLimit(2L);

		ResourceList<RelatedEntity> list = relRepo.findManyTargets(test.getId(), TestEntity.ATTR_manyRelatedValues, querySpec);
		Assert.assertEquals(2, list.size());
		Assert.assertEquals(2, list.get(0).getId().intValue());
		Assert.assertEquals(3, list.get(1).getId().intValue());

		JsonMetaInformation meta = list.getMeta(JsonMetaInformation.class);
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		Assert.assertNull(meta);
		Assert.assertNotNull(links);

		String baseUri = getBaseUri().toString();
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues?page[limit]=2",
				links.asJsonNode().get("first").asText());
		Assert.assertNull(links.asJsonNode().get("last"));
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues?page[limit]=2",
				links.asJsonNode().get("prev").asText());
		Assert.assertEquals(baseUri + "test/1/relationships/manyRelatedValues?page[limit]=2&page[offset]=4",
				links.asJsonNode().get("next").asText());
	}
}
