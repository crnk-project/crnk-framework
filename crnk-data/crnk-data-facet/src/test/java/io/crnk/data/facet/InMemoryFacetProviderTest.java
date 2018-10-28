package io.crnk.data.facet;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.facet.internal.FacetRepositoryImpl;
import io.crnk.data.facet.setup.FacetTestSetup;
import io.crnk.test.mock.TestModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class InMemoryFacetProviderTest {

	private FacetTestSetup setup;

	private FacetRepositoryImpl repository;

	@Before
	public void setup() {
		setup = new FacetTestSetup();
		setup.boot();
		repository = setup.getRepository();
	}

	@After
	public void teardown() {
		TestModule.clear();
	}

	@Test
	public void checkFindAll() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());

		FacetResource nameFacets = list.get(0);
		Assert.assertEquals("projects_name", nameFacets.getId());
		Assert.assertEquals("projects", nameFacets.getType());
		Assert.assertEquals("name", nameFacets.getName());
		Assert.assertEquals(Arrays.asList("project3", "project2", "project1", "project0"), nameFacets.getLabels());
		Map<String, FacetValue> values = nameFacets.getValues();
		FacetValue value0 = values.get("project0");
		Assert.assertEquals(1, value0.getCount());
		FacetValue value1 = values.get("project1");
		Assert.assertEquals(3, value1.getCount());
		FacetValue value2 = values.get("project2");
		Assert.assertEquals(5, value2.getCount());
		FacetValue value3 = values.get("project3");
		Assert.assertEquals(7, value3.getCount());
		Assert.assertEquals("project3", value3.getLabel());
		Assert.assertEquals(PathSpec.of("name").filter(FilterOperator.EQ, "project3"), value3.getFilterSpec());
		Assert.assertEquals("project3", value3.getValue());

		FacetResource priorityFacet = list.get(1);
		Assert.assertEquals("projects_priority", priorityFacet.getId());
		Assert.assertEquals("projects", priorityFacet.getType());
		Assert.assertEquals("priority", priorityFacet.getName());
		Assert.assertEquals(2, priorityFacet.getValues().size());
	}


	@Test
	public void checkFacetOrder1() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("name", "priority")));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());

		FacetResource nameFacets = list.get(0);
		Assert.assertEquals("projects", nameFacets.getType());
		Assert.assertEquals("name", nameFacets.getName());

		FacetResource priorityFacet = list.get(1);
		Assert.assertEquals("projects", priorityFacet.getType());
		Assert.assertEquals("priority", priorityFacet.getName());
	}

	@Test
	public void checkFacetOrder2() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("priority", "name")));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());

		FacetResource priorityFacet = list.get(0);
		Assert.assertEquals("projects", priorityFacet.getType());
		Assert.assertEquals("priority", priorityFacet.getName());

		FacetResource nameFacets = list.get(1);
		Assert.assertEquals("projects", nameFacets.getType());
		Assert.assertEquals("name", nameFacets.getName());
	}


	@Test
	public void checkNestedFacetFilter() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("priority", "name")));
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_VALUES, "priority").filter(FilterOperator.SELECT, "1"));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());

		FacetResource priorityFacet = list.get(0);
		Assert.assertEquals("projects", priorityFacet.getType());
		Assert.assertEquals("priority", priorityFacet.getName());
		Assert.assertEquals(2, priorityFacet.getValues().size());

		// filtered by priority, counts reduced accordingly
		FacetResource nameFacets = list.get(1);
		Assert.assertEquals("projects", nameFacets.getType());
		Assert.assertEquals("name", nameFacets.getName());

		Map<String, FacetValue> values = nameFacets.getValues();
		FacetValue value1 = values.get("project1");
		Assert.assertEquals(2, value1.getCount());
		FacetValue value2 = values.get("project2");
		Assert.assertEquals(2, value2.getCount());
		FacetValue value3 = values.get("project3");
		Assert.assertEquals(4, value3.getCount());
	}

	@Test
	public void matchFilterByType() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("type").filter(FilterOperator.EQ, "projects"));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void mismatchFilterByType() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("type").filter(FilterOperator.EQ, "doesNotExist"));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(0, list.size());
	}

	@Test
	public void matchFilterByFacet() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "name"));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
	}

	@Test
	public void mismatchFilterByFacet() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "doesNotExist"));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(0, list.size());
	}
}
