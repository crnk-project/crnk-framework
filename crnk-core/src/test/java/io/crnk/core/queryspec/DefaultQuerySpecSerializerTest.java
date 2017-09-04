package io.crnk.core.queryspec;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DefaultQuerySpecSerializerTest {

	private JsonApiUrlBuilder urlBuilder;

	private ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://127.0.0.1"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.boot();

		resourceRegistry = boot.getResourceRegistry();
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
	}

	@Test
	public void testHttpsSchema() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("https://127.0.0.1"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.boot();

		urlBuilder = new JsonApiUrlBuilder(boot.getResourceRegistry());
		check("https://127.0.0.1/tasks/", null, new QuerySpec(Task.class));
	}

	@Test
	public void testPort() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("https://127.0.0.1:1234"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.boot();

		urlBuilder = new JsonApiUrlBuilder(boot.getResourceRegistry());
		check("https://127.0.0.1:1234/tasks/", null, new QuerySpec(Task.class));
	}

	@Test(expected = RepositoryNotFoundException.class)
	public void unknownResourceShouldThrowException() throws InstantiationException, IllegalAccessException {
		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		Class<?> notAResourceClass = String.class;
		urlBuilder.buildUrl(entry.getResourceInformation(), null, new QuerySpec(notAResourceClass));
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/", null, new QuerySpec(Task.class));
	}

	@Test
	public void testFilterNonRootType() throws InstantiationException, IllegalAccessException {
		QuerySpec projectQuerySpec = new QuerySpec(Project.class);
		projectQuerySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test"));
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.putRelatedSpec(Project.class, projectQuerySpec);
		check("http://127.0.0.1/tasks/?filter[projects][name][EQ]=test", null, querySpec);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNestedFilterSpecNotYetSupported() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(FilterSpec.or(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test"), new FilterSpec(Arrays.asList("name"), FilterOperator.GE, "test")));

		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		urlBuilder.buildUrl(entry.getResourceInformation(), null, querySpec);
	}

	@Test
	public void testFindById() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1/", 1, new QuerySpec(Task.class));
	}

	@Test
	public void testFindByIds() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1,2,3/", Arrays.asList(1, 2, 3), new QuerySpec(Task.class));
	}

	@Test
	public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		check("http://127.0.0.1/tasks/?sort[tasks]=name", null, querySpec);
	}

	@Test
	public void testFindAllOrderMultipleFields() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		querySpec.addSort(new SortSpec(Arrays.asList("id"), Direction.DESC));
		check("http://127.0.0.1/tasks/?sort[tasks]=name%2C-id", null, querySpec);
	}

	@Test
	public void testFindAllIncludeMultipleFields() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeField(Arrays.asList("name"));
		querySpec.includeField(Arrays.asList("id"));
		check("http://127.0.0.1/tasks/?fields[tasks]=name%2Cid", null, querySpec);
	}

	@Test
	public void testFindAllIncludeMultipleRelations() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));
		querySpec.includeRelation(Arrays.asList("projects"));
		check("http://127.0.0.1/tasks/?include[tasks]=project%2Cprojects", null, querySpec);
	}

	@Test
	public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));
		check("http://127.0.0.1/tasks/?sort[tasks]=-name", null, querySpec);
	}

	@Test
	public void testFilterByOne() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));
		check("http://127.0.0.1/tasks/?filter[tasks][name][EQ]=value", null, querySpec);
	}

	@Test
	public void testFilterByPath() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, "value"));
		check("http://127.0.0.1/tasks/?filter[tasks][project.name][EQ]=value", null, querySpec);
	}

	@Test
	public void testFilterByMany() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, Arrays.asList("value1", "value2")));

		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		String actualUrl = urlBuilder.buildUrl(entry.getResourceInformation(), null, querySpec);
		String expectedUrl0 = "http://127.0.0.1/tasks/?filter[tasks][name][EQ]=value2&filter[tasks][name][EQ]=value1";
		String expectedUrl1 = "http://127.0.0.1/tasks/?filter[tasks][name][EQ]=value1&filter[tasks][name][EQ]=value2";

		Assert.assertTrue(expectedUrl0.equals(actualUrl) || expectedUrl1.equals(actualUrl));
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 1));
		check("http://127.0.0.1/tasks/?filter[tasks][id][EQ]=1", null, querySpec);
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1));
		check("http://127.0.0.1/tasks/?filter[tasks][id][LE]=1", null, querySpec);
	}

	//
	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.setLimit(2L);
		querySpec.setOffset(1L);
		check("http://127.0.0.1/tasks/?page[limit]=2&page[offset]=1", null, querySpec);
	}

	@Test
	public void testPagingOnRelation() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.setLimit(2L);
		querySpec.setOffset(1L);

		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		String actualUrl = urlBuilder.buildUrl(entry.getResourceInformation(), 1L, querySpec, "projects");
		assertEquals("http://127.0.0.1/tasks/1/relationships/projects/?page[limit]=2&page[offset]=1", actualUrl);
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeRelation(Arrays.asList("project"));
		check("http://127.0.0.1/tasks/?include[tasks]=project", null, querySpec);
	}

	@Test
	public void testIncludeAttributes() throws InstantiationException, IllegalAccessException {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.includeField(Arrays.asList("name"));
		check("http://127.0.0.1/tasks/?fields[tasks]=name", null, querySpec);
	}

	private void check(String expectedUrl, Object id, QuerySpec querySpec) {
		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		String actualUrl = urlBuilder.buildUrl(entry.getResourceInformation(), id, querySpec);
		assertEquals(expectedUrl, actualUrl);
	}
}
