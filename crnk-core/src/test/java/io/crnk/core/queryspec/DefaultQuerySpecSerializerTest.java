package io.crnk.core.queryspec;

import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.DefaultResourceLookup;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import io.crnk.legacy.locator.JsonServiceLocator;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DefaultQuerySpecSerializerTest {

	private ModuleRegistry moduleRegistry = new ModuleRegistry();
	private JsonApiUrlBuilder urlBuilder;

	private DefaultResourceLookup resourceLookup;

	private ResourceRegistryBuilder resourceRegistryBuilder;

	private ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		JsonServiceLocator jsonServiceLocator = new SampleJsonServiceLocator();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		resourceRegistryBuilder = new ResourceRegistryBuilder(moduleRegistry, jsonServiceLocator, resourceInformationBuilder);
		resourceLookup = new DefaultResourceLookup(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE);
		resourceRegistry = resourceRegistryBuilder.build(resourceLookup, moduleRegistry, new ConstantServiceUrlProvider("http://127.0.0.1"));
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
	}

	@Test
	public void testHttpsSchema() {
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup, moduleRegistry, new ConstantServiceUrlProvider("https://127.0.0.1"));
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
		check("https://127.0.0.1/tasks/", null, new QuerySpec(Task.class));
	}

	@Test
	public void testPort() {
		ResourceRegistry resourceRegistry = resourceRegistryBuilder.build(resourceLookup, moduleRegistry, new ConstantServiceUrlProvider("https://127.0.0.1:1234"));
		urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
		check("https://127.0.0.1:1234/tasks/", null, new QuerySpec(Task.class));
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/", null, new QuerySpec(Task.class));
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

		RegistryEntry entry = resourceRegistry.findEntry(Task.class);
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

		RegistryEntry entry = resourceRegistry.findEntry(Task.class);
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
		RegistryEntry entry = resourceRegistry.findEntry(Task.class);
		String actualUrl = urlBuilder.buildUrl(entry.getResourceInformation(), id, querySpec);
		assertEquals(expectedUrl, actualUrl);
	}
}
