package io.crnk.jpa.integration;

import io.crnk.client.response.JsonLinksInformation;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingBehavior;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.model.TestEntity;
import io.crnk.rs.CrnkFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

public class JpaPageNumberIntTest extends AbstractJpaJerseyTest {

	private CrnkBoot boot;

	@Override
	@Before
	public void setup() {
		super.setup();
		client.addModule(NumberSizePagingBehavior.createModule());
	}

	@Override
	protected void setupFeature(CrnkFeature feature) {
		boot = feature.getBoot();
		feature.addModule(NumberSizePagingBehavior.createModule());
	}

	@Test
	public void test() {
		Assert.assertEquals(1, boot.getPagingBehaviors().size());
		Assert.assertTrue(boot.getPagingBehaviors().get(0) instanceof NumberSizePagingBehavior);

		ResourceRepository<TestEntity, Serializable> repository = client.getRepositoryForType(TestEntity.class);
		for (int i = 0; i < 20; i++) {
			TestEntity test = new TestEntity();
			test.setId((long) i);
			test.setLongValue((long) i);
			repository.create(test);
		}

		QuerySpec querySpec = new QuerySpec(TestEntity.class);
		querySpec.setPaging(new NumberSizePagingSpec(2, 5));
		ResourceList<TestEntity> list = repository.findAll(querySpec);
		Assert.assertEquals(5, list.size());

		String url = client.getServiceUrlProvider().getUrl();
		JsonLinksInformation links = list.getLinks(JsonLinksInformation.class);
		Assert.assertEquals(url + "/test?page[number]=1&page[size]=5", links.asJsonNode().get("first").asText());
		Assert.assertEquals(url + "/test?page[number]=1&page[size]=5", links.asJsonNode().get("prev").asText());
		Assert.assertEquals(url + "/test?page[number]=3&page[size]=5", links.asJsonNode().get("next").asText());
		Assert.assertEquals(url + "/test?page[number]=4&page[size]=5", links.asJsonNode().get("last").asText());
	}
}
