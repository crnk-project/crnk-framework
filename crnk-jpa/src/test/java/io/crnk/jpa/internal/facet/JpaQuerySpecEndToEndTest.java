package io.crnk.jpa.internal.facet;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.facet.FacetResource;
import io.crnk.data.facet.FacetValue;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.JpaModule;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public class JpaQuerySpecEndToEndTest extends AbstractJpaJerseyTest {

	private ResourceRepositoryV2<TestEntity, Long> testRepo;

	private ResourceRepositoryV2<FacetResource, Serializable> facetRepository;


	@Override
	@Before
	public void setup() {
		super.setup();
		testRepo = client.getRepositoryForType(TestEntity.class);
		facetRepository = client.getRepositoryForType(FacetResource.class);
		for (int i = 0; i < 16; i++) {
			TestEntity entity = new TestEntity();
			entity.setId((long) i);
			entity.setLongValue((long) Math.sqrt(i));
			testRepo.create(entity);
		}
	}

	@Override
	protected void setupModule(JpaModule module, boolean server) {
		if (server) {
			module.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
		}
	}

	@Test
	public void checkFindAll() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		ResourceList<FacetResource> list = facetRepository.findAll(querySpec);
		Assert.assertEquals(1, list.size());

		FacetResource nameFacets = list.get(0);
		Assert.assertEquals("test_longValue", nameFacets.getId());
		Assert.assertEquals("test", nameFacets.getType());
		Assert.assertEquals("longValue", nameFacets.getName());
		Assert.assertEquals(Arrays.asList("3", "2", "1", "0"), nameFacets.getLabels());
		Map<String, FacetValue> values = nameFacets.getValues();
		FacetValue value0 = values.get("0");
		Assert.assertEquals(1, value0.getCount());
		FacetValue value1 = values.get("1");
		Assert.assertEquals(3, value1.getCount());
		FacetValue value2 = values.get("2");
		Assert.assertEquals(5, value2.getCount());
		FacetValue value3 = values.get("3");
		Assert.assertEquals(7, value3.getCount());
		Assert.assertEquals("3", value3.getLabel());
		Assert.assertEquals(PathSpec.of("longValue").filter(FilterOperator.EQ, 3), value3.getFilterSpec());
		Assert.assertEquals(3, value3.getValue());
	}
}
