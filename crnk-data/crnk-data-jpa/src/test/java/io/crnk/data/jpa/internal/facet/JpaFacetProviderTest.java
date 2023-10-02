package io.crnk.data.jpa.internal.facet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.facet.FacetResource;
import io.crnk.data.facet.FacetValue;
import io.crnk.data.facet.config.FacetInformation;
import io.crnk.data.jpa.AbstractJpaJerseyTest;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import jakarta.persistence.EntityManager;

public class JpaFacetProviderTest extends AbstractJpaJerseyTest {

	private ResourceRepository<TestEntity, Long> testRepo;

	private ResourceRepository<FacetResource, Serializable> facetRepository;


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
	protected void setupModule(JpaModuleConfig config, boolean server, EntityManager em) {
		if (server) {
			config.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
		}
	}

	@Test
	public void checkFindAll() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		ResourceList<FacetResource> list = facetRepository.findAll(querySpec);
		Assert.assertEquals(1, list.size());

		FacetResource nameFacets = list.get(0);
		Assert.assertEquals("test_longValue", nameFacets.getId());
		Assert.assertEquals("test", nameFacets.getResourceType());
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

	@Test(expected = UnsupportedOperationException.class)
	public void checkUnknownFacet() {
		JpaFacetProvider facetProvider = new JpaFacetProvider();
		QuerySpec querySpec = Mockito.mock(QuerySpec.class);
		FacetInformation facetInformation = Mockito.mock(FacetInformation.class);
		facetProvider.findValues(facetInformation, querySpec);
	}

}
