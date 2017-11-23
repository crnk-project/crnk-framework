package io.crnk.core.queryspec;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class DefaultQuerySpecConverterTest extends AbstractQuerySpecTest {

	@Test
	public void testFindAll() throws InstantiationException, IllegalAccessException {
		QuerySpec spec = querySpecConverter.fromParams(Task.class, new QueryParams());
		Assert.assertEquals(new QuerySpec(Task.class), spec);
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
		addParams(params, "sort[tasks][name]", asc ? "asc" : "desc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<SortSpec> sort = spec.getSort();
		Assert.assertEquals(1, sort.size());
		Assert.assertEquals(Arrays.asList("name"), sort.get(0).getAttributePath());
		Assert.assertEquals(asc ? Direction.ASC : Direction.DESC, sort.get(0).getDirection());
	}

	@Test
	public void testFilterString() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[tasks][name]", "test1");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<FilterSpec> filters = spec.getFilters();
		Assert.assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		Assert.assertEquals(Arrays.asList("name"), filter.getAttributePath());
		Assert.assertEquals("test1", filter.getValue());
	}

	@Test
	public void testFilterLong() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[tasks][id]", "12");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<FilterSpec> filters = spec.getFilters();
		Assert.assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		Assert.assertEquals(Arrays.asList("id"), filter.getAttributePath());
		Assert.assertEquals(FilterOperator.EQ, filter.getOperator());
		Assert.assertEquals(Long.valueOf(12L), filter.getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFilterUnknownResource() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[unknown][id]", "12");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
		querySpecConverter.fromParams(Task.class, queryParams);
	}

	@Test
	public void testNestedRelationFilter() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[tasks][project][name]", "myProject");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<FilterSpec> filters = spec.getFilters();
		Assert.assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		Assert.assertEquals(Arrays.asList("project", "name"), filter.getAttributePath());
		Assert.assertEquals(FilterOperator.EQ, filter.getOperator());
		Assert.assertEquals("myProject", filter.getValue());
	}

	@Test
	public void testNestedObjectFilter() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[projects][data][data]", "myData");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Project.class, queryParams);
		List<FilterSpec> filters = spec.getFilters();
		Assert.assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		Assert.assertEquals(Arrays.asList("data", "data"), filter.getAttributePath());
		Assert.assertEquals(FilterOperator.EQ, filter.getOperator());
		Assert.assertEquals("myData", filter.getValue());
	}

	@Test
	public void testFilterNEQ() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[tasks][id][NEQ]", "12");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<FilterSpec> filters = spec.getFilters();
		Assert.assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		Assert.assertEquals(Arrays.asList("id"), filter.getAttributePath());
		Assert.assertEquals(FilterOperator.NEQ, filter.getOperator());
		Assert.assertEquals(Long.valueOf(12L), filter.getValue());
	}

	@Test
	public void testFilterLike() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "filter[tasks][name][GE]", "myTask");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<FilterSpec> filters = spec.getFilters();
		Assert.assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		Assert.assertEquals(Arrays.asList("name"), filter.getAttributePath());
		Assert.assertEquals(FilterOperator.GE, filter.getOperator());
		Assert.assertEquals("myTask", filter.getValue());
	}

	@Test
	public void testNestedSort() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "sort[tasks][project][name]", "asc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<SortSpec> sorts = spec.getSort();
		Assert.assertEquals(1, sorts.size());
		SortSpec sort = sorts.get(0);
		Assert.assertEquals(Arrays.asList("project", "name"), sort.getAttributePath());
		Assert.assertEquals(Direction.ASC, sort.getDirection());
	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "page[offset]", "1");
		addParams(params, "page[limit]", "2");
		addParams(params, "sort[tasks][id]", "asc");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		Assert.assertEquals(1L, spec.getOffset());
		Assert.assertEquals(Long.valueOf(2L), spec.getLimit());
	}

	@Test
	public void testIncludeField() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "fields[tasks]", "name");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<IncludeFieldSpec> includes = spec.getIncludedFields();
		Assert.assertEquals(1, includes.size());
		IncludeFieldSpec include = includes.get(0);
		Assert.assertEquals(Arrays.asList("name"), include.getAttributePath());
	}

	@Test
	public void testIncludeRelations() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<String, Set<String>>();
		addParams(params, "include[tasks]", "project");
		QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);

		QuerySpec spec = querySpecConverter.fromParams(Task.class, queryParams);
		List<IncludeRelationSpec> includes = spec.getIncludedRelations();
		Assert.assertEquals(1, includes.size());
		IncludeRelationSpec include = includes.get(0);
		Assert.assertEquals(Arrays.asList("project"), include.getAttributePath());
	}
}
