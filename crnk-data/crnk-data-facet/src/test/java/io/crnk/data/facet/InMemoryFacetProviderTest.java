package io.crnk.data.facet;

import java.util.Arrays;
import java.util.Map;

import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.facet.internal.FacetRepositoryImpl;
import io.crnk.data.facet.setup.FacetTestSetup;
import io.crnk.data.facet.setup.FacetedProject;
import io.crnk.data.facet.setup.FacetedProjectRepository;
import io.crnk.data.facet.setup.FacetedTask;
import io.crnk.test.mock.TestModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
		Assert.assertEquals("projects", nameFacets.getResourceType());
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
		Assert.assertEquals("projects", priorityFacet.getResourceType());
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
		Assert.assertEquals("projects", nameFacets.getResourceType());
		Assert.assertEquals("name", nameFacets.getName());

		FacetResource priorityFacet = list.get(1);
		Assert.assertEquals("projects", priorityFacet.getResourceType());
		Assert.assertEquals("priority", priorityFacet.getName());
	}

	@Test
	public void checkFacetOrder2() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("priority", "name")));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());

		FacetResource priorityFacet = list.get(0);
		Assert.assertEquals("projects", priorityFacet.getResourceType());
		Assert.assertEquals("priority", priorityFacet.getName());

		FacetResource nameFacets = list.get(1);
		Assert.assertEquals("projects", nameFacets.getResourceType());
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
		Assert.assertEquals("projects", priorityFacet.getResourceType());
		Assert.assertEquals("priority", priorityFacet.getName());
		Assert.assertEquals(2, priorityFacet.getValues().size());

		// filtered by priority, counts reduced accordingly
		FacetResource nameFacets = list.get(1);
		Assert.assertEquals("projects", nameFacets.getResourceType());
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
	public void checkNestedFacetFilterAllSelected() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_NAME).filter(FilterOperator.EQ, Arrays.asList("priority", "name")));
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_VALUES, "priority").filter(FilterOperator.SELECT, Arrays.asList("0", "1")));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());

		FacetResource priorityFacet = list.get(0);
		Assert.assertEquals("projects", priorityFacet.getResourceType());
		Assert.assertEquals("priority", priorityFacet.getName());
		Assert.assertEquals(2, priorityFacet.getValues().size());

		// filtered by priority, counts reduced accordingly
		FacetResource nameFacets = list.get(1);
		Assert.assertEquals("projects", nameFacets.getResourceType());
		Assert.assertEquals("name", nameFacets.getName());

		Map<String, FacetValue> values = nameFacets.getValues();
		FacetValue value1 = values.get("project1");
		Assert.assertEquals(3, value1.getCount());
		FacetValue value2 = values.get("project2");
		Assert.assertEquals(5, value2.getCount());
		FacetValue value3 = values.get("project3");
		Assert.assertEquals(7, value3.getCount());
	}

	@Test
	public void checkGrouping() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("values").filter(FilterOperator.GROUP, "priority"));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(4, list.size());

		// groups must come first
		FacetResource priorityFacet0 = list.get(0);
		Assert.assertEquals("projects_priority_0", priorityFacet0.getId());
		Assert.assertEquals("projects", priorityFacet0.getResourceType());
		Assert.assertEquals("priority", priorityFacet0.getName());
		Assert.assertEquals(Arrays.asList("0"), priorityFacet0.getLabels());
		Assert.assertEquals("0", priorityFacet0.getGroups().get("priority"));
		Assert.assertEquals(8, priorityFacet0.getValues().get("0").getCount());

		FacetResource priorityFacet1 = list.get(1);
		Assert.assertEquals("projects_priority_1", priorityFacet1.getId());
		Assert.assertEquals("projects", priorityFacet1.getResourceType());
		Assert.assertEquals("priority", priorityFacet1.getName());
		Assert.assertEquals(Arrays.asList("1"), priorityFacet1.getLabels());
		Assert.assertEquals("1", priorityFacet1.getGroups().get("priority"));
		Assert.assertEquals(8, priorityFacet1.getValues().get("1").getCount());

		FacetResource nameFacet = list.get(2);
		Assert.assertEquals("name", nameFacet.getName());
		Assert.assertEquals(4, nameFacet.getValues().size());
		Assert.assertTrue(nameFacet.getValues().containsKey("project0"));
		Assert.assertTrue(nameFacet.getValues().containsKey("project1"));
		Assert.assertTrue(nameFacet.getValues().containsKey("project2"));
		Assert.assertTrue(nameFacet.getValues().containsKey("project3"));

	}

	@Test
	public void checkNull() {
		RegistryEntry entry = setup.getBoot().getResourceRegistry().getEntry(FacetedProject.class);
		FacetedProjectRepository projectRepository = (FacetedProjectRepository) entry.getResourceRepository().getImplementation();
		projectRepository.clear();

		FacetedProject project = new FacetedProject();
		project.setId(12L);
		project.setPriority(1);
		projectRepository.create(project);

		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		ResourceList<FacetResource> list = repository.findAll(querySpec);

		FacetResource nameFacet = list.get(0);
		Assert.assertEquals("name", nameFacet.getName());
		Assert.assertEquals(1, nameFacet.getValues().size());
		Assert.assertTrue(nameFacet.getValues().containsKey("null"));
		FacetValue value = nameFacet.getValues().get("null");
		Assert.assertEquals(1, value.getCount());
		Assert.assertEquals(PathSpec.of("name").filter(FilterOperator.EQ, null), value.getFilterSpec());
	}

	@Test
	public void checkNotExposedNotFacetted() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of(FacetResource.ATTR_RESOURCE_TYPE).filter(FilterOperator.EQ, FacetedTask.RESOURCE_TYPE));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void checkNestedGrouping() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("values").filter(FilterOperator.GROUP, Arrays.asList("name", "priority")));
		querySpec.addSort(PathSpec.of("name").sort(Direction.ASC));
		querySpec.addSort(PathSpec.of("id").sort(Direction.ASC));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(11, list.size());

		// 0, 1, 1, 1, 2, 2, 2, 2, 2, 3 => Math.sqrt in setup
		int[] counts = { 1, 3, 5, 7 };

		for (int i = 0; i < 3; i++) {
			FacetResource nameFacet = list.get(i);
			String name = nameFacet.getLabels().get(0);
			Assert.assertEquals(1, nameFacet.getGroups().size());
			Assert.assertEquals(1, nameFacet.getLabels().size());
			Assert.assertEquals(1, nameFacet.getValues().size());
			Assert.assertEquals("projects_name_" + name, nameFacet.getId());
			int expectedCount = counts[i];
			Assert.assertEquals(expectedCount, nameFacet.getValues().get(name).getCount());
		}


		for (int i = 0; i < 7; i++) {
			FacetResource priorityFacet = list.get(4 + i);

			Assert.assertEquals(1, priorityFacet.getLabels().size());
			Assert.assertEquals(1, priorityFacet.getValues().size());
			Assert.assertEquals(2, priorityFacet.getGroups().size());
			int group = Integer.parseInt(priorityFacet.getGroups().get("name").substring("project".length()));
			String priority = priorityFacet.getLabels().get(0);
			Assert.assertTrue(priorityFacet.getGroups().containsKey("priority"));
			long count = priorityFacet.getValues().get(priority).getCount();
			Assert.assertEquals("projects_name_project" + group + "_priority_" + priority, priorityFacet.getId());

			Assert.assertTrue(count == counts[group] / 2 || count == counts[group] / 2 + 1);
		}
	}


	@Test(expected = BadRequestException.class)
	public void checkInvalidGroupNameThrowsException() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("values").filter(FilterOperator.GROUP, Arrays.asList("doesNotExist", "priority")));
		querySpec.addSort(PathSpec.of("name").sort(Direction.ASC));
		querySpec.addSort(PathSpec.of("id").sort(Direction.ASC));
		repository.findAll(querySpec);
	}


	@Test
	public void matchFilterByType() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("resourceType").filter(FilterOperator.EQ, "projects"));
		ResourceList<FacetResource> list = repository.findAll(querySpec);
		Assert.assertEquals(2, list.size());
	}

	@Test
	public void mismatchFilterByType() {
		QuerySpec querySpec = new QuerySpec(FacetResource.class);
		querySpec.addFilter(PathSpec.of("resourceType").filter(FilterOperator.EQ, "doesNotExist"));
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
