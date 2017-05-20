package io.crnk.legacy.queryParams;

import io.crnk.core.engine.information.resource.ResourceInformationBuilderContext;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.module.TestResource;
import io.crnk.core.module.TestResourceInformationBuilder;
import io.crnk.legacy.queryParams.context.SimpleQueryParamsParserContext;
import io.crnk.legacy.queryParams.include.Inclusion;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultQueryParamsParserTest {

	private Map<String, Set<String>> queryParams;
	private QueryParamsParser parser = new DefaultQueryParamsParser();

	@Before
	public void prepare() {
		queryParams = new HashMap<>();
	}

	@Test
	public void onGivenFiltersParserShouldReturnOnlyRequestParamsWithFilters() {
		// GIVEN
		queryParams.put("filter[users][name]", Collections.singleton("John"));
		queryParams.put("random[users][name]", Collections.singleton("John"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getFilters().getParams().size()).isEqualTo(1);
		assertThat(result.getFilters().getParams().get("users").getParams().size()).isEqualTo(1);
		assertThat(result.getFilters().getParams().get("users").getParams().get("name")).isEqualTo(Collections.singleton("John"));
	}

	@Test
	public void onGivenSortingParserShouldReturnOnlyRequestParamsWithSorting() {
		// GIVEN
		queryParams.put("sort[users][name]", Collections.singleton("asc"));
		queryParams.put("random[users][name]", Collections.singleton("desc"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getSorting().getParams().size()).isEqualTo(1);
		assertThat(result.getSorting().getParams().get("users").getParams().size()).isEqualTo(1);
		assertThat(result.getSorting().getParams().get("users").getParams().get("name")).isEqualTo(RestrictedSortingValues.asc);
	}

	@Test
	public void onGivenGroupingParserShouldReturnOnlyRequestParamsWithGrouping() {
		// GIVEN
		queryParams.put("group[users]", Collections.singleton("name"));
		queryParams.put("random[users]", Collections.singleton("surname"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getGrouping().getParams().size()).isEqualTo(1);
		assertThat(result.getGrouping().getParams()).containsOnlyKeys("users");
	}

	@Test
	public void onGivenPaginationParserShouldReturnOnlyRequestParamsWithPagination() {
		// GIVEN
		queryParams.put("page[offset]", Collections.singleton("1"));
		queryParams.put("page[limit]", Collections.singleton("10"));
		queryParams.put("random[offset]", Collections.singleton("2"));
		queryParams.put("random[limit]", Collections.singleton("20"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getPagination().size()).isEqualTo(2);
		assertThat(result.getPagination().get(RestrictedPaginationKeys.offset)).isEqualTo(1);
		assertThat(result.getPagination().get(RestrictedPaginationKeys.limit)).isEqualTo(10);
	}

	@Test
	public void onGivenIncludedFieldsParserShouldReturnOnlyRequestParamsWithIncludedFields() {
		// GIVEN
		queryParams.put("fields[users]", Collections.singleton("name"));
		queryParams.put("random[users]", Collections.singleton("surname"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getIncludedFields().getParams().size()).isEqualTo(1);
		assertThat(result.getIncludedFields().getParams().get("users").getParams()).containsOnly("name");
	}

	@Test
	public void onGivenIncludedRelationsParserShouldReturnOnlyRequestParamsWithIncludedRelations() {
		// GIVEN
		queryParams.put("include[user]", Collections.singleton("name"));
		queryParams.put("random[user]", Collections.singleton("surname"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getIncludedRelations().getParams().size()).isEqualTo(1);
		assertThat(result.getIncludedRelations().getParams().get("user").getParams()).containsOnly(new Inclusion("name"));
	}

	private QueryParams parseQueryParams() {
		TestResourceInformationBuilder infoBuilder = new TestResourceInformationBuilder();
		infoBuilder.init(new ResourceInformationBuilderContext() {

			@Override
			public String getResourceType(Class<?> clazz) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean accept(Class<?> type) {
				throw new UnsupportedOperationException();
			}

			@Override
			public TypeParser getTypeParser() {
				return new TypeParser();
			}
		});
		return parser.parse(new SimpleQueryParamsParserContext(queryParams, infoBuilder.build(TestResource.class)));
	}
}
