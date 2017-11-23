package io.crnk.legacy.queryParams;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import org.junit.Before;
import org.junit.Test;

public class DefaultQueryParamsSerializerTest {

	private QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
	private JsonApiUrlBuilder urlBuilder;
	private ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://127.0.0.1"));
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
		check("https://127.0.0.1/tasks", null, new QueryParams());
	}

	@Test
	public void testPort() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("https://127.0.0.1:1234"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.boot();
		resourceRegistry = boot.getResourceRegistry();
		urlBuilder = new JsonApiUrlBuilder(boot.getResourceRegistry());
		check("https://127.0.0.1:1234/tasks", null, new QueryParams());
	}

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks", null, new QueryParams());
	}

	@Test
	public void testFindById() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1", 1, new QueryParams());
	}

	@Test
	public void testFindByIds() throws InstantiationException, IllegalAccessException {
		check("http://127.0.0.1/tasks/1,2,3", Arrays.asList(1, 2, 3), new QueryParams());
	}

	@Test
	public void testFindAllOrderByAsc() throws InstantiationException, IllegalAccessException {
		testFindAllOrder(true);
	}

	@Test
	public void testFindAllOrderByDesc() throws InstantiationException, IllegalAccessException {
		testFindAllOrder(false);
	}

	public void testFindAllOrder(boolean asc) throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		String dir = asc ? "asc" : "desc";
		addParams(params, "sort[test][longValue]", dir);
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks?sort[test][longValue]=" + dir, null, queryParams);
	}

	@Test
	public void testFilterByOne() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][stringValue]", "value");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks?filter[test][stringValue]=value", null, queryParams);
	}

	@Test
	public void testFilterByMany() throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][stringValue]", "value0,value1");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks?filter[test][stringValue]=" + URLEncoder.encode("value0,value1", "UTF-8"), null, queryParams);
	}

	@Test
	public void testFilterEquals() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][longValue][equal]", "1");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks?filter[test][longValue][equal]=1", null, queryParams);
	}

	@Test
	public void testFilterGreater() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][longValue][greater]", "1");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks?filter[test][longValue][greater]=1", null, queryParams);
	}

	@Test
	public void testFilterLike() throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[test][longValue][like]", "test%");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		check("http://127.0.0.1/tasks?filter[test][longValue][like]=" + URLEncoder.encode("test%", "UTF-8"), null,
				queryParams);
	}

	//
	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "page[offset]", "1");
		addParams(params, "page[limit]", "2");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		check("http://127.0.0.1/tasks?page[limit]=2&page[offset]=1", null, queryParams);
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[test]", "project");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		check("http://127.0.0.1/tasks?include[test]=project", null, queryParams);
	}

	@Test
	public void testIncludeAttributes() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "fields[test]", "project");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		check("http://127.0.0.1/tasks?fields[test]=project", null, queryParams);
	}

	private void check(String expectedUrl, Object id, QueryParams queryParams) {
		RegistryEntry entry = resourceRegistry.getEntryForClass(Task.class);
		String actualUrl = urlBuilder.buildUrl(entry.getResourceInformation(), id, queryParams);

		assertEquals(expectedUrl, actualUrl);
	}

	private void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Collections.singletonList(value)));
	}
}
