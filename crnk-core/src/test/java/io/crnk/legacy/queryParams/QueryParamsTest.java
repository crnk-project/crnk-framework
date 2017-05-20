package io.crnk.legacy.queryParams;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QueryParamsTest extends AbstractQueryParamsTest {

	QueryParams first;
	QueryParams second;
	QueryParams other;

	/*
		This function returns the same map, either a full or partial version of it
        We do this to avoid re-using the same underlying map object just to ensure
        the equals/hashcode implementation is correct.
     */
	private Map<String, Set<String>> params(boolean partial) {
		Map<String, Set<String>> params = new HashMap<>();
		addParams(params, "page[offset]", "1");
		addParams(params, "page[limit]", "2");
		addParams(params, "sort[tasks][project][name]", "asc");
		addParams(params, "filter[tasks][name]", "test1");
		addParams(params, "group[tasks]", "test", "name");
		if (partial) return params;
		addParams(params, "include[tasks]", "project", "projects");
		addParams(params, "fields[tasks]", "name", "category");
		addParams(params, "filter[tasks][name][GE]", "myTask");
		return params;
	}

	@Before
	public void setup() {
		other = queryParamsBuilder.buildQueryParams(params(true));
		first = queryParamsBuilder.buildQueryParams(params(false));
		second = queryParamsBuilder.buildQueryParams(params(false));
	}

	@Test
	public void testHashCode() {
		Assert.assertEquals("first.hashCode == second.hashCode", first.hashCode(), second.hashCode());
		Assert.assertNotEquals("other.hashCode != first.hashCode", other.hashCode(), first.hashCode());
	}

	@Test
	public void testEqualsBlank() {
		QueryParams blank1 = queryParamsBuilder.buildQueryParams(new HashMap<String, Set<String>>());
		QueryParams blank2 = queryParamsBuilder.buildQueryParams(new HashMap<String, Set<String>>());

		Assert.assertTrue("blank1 == blank1", blank1.equals(blank1));
		Assert.assertTrue("blank1 == blank2", blank1.equals(blank2));
		Assert.assertTrue("blank2 == blank1", blank2.equals(blank1));
	}

	@Test
	public void testEquals() {
		Assert.assertTrue("first == first", first.equals(first));
		Assert.assertTrue("first == second", first.equals(second));
		Assert.assertTrue("second == first", second.equals(first));
		Assert.assertTrue("other == other", other.equals(other));

		Assert.assertFalse("first != other", first.equals(other));
		Assert.assertFalse("other != second", other.equals(second));
	}
}