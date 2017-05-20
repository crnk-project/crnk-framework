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

public class JsonApiQueryParamsParserTest {

	private Map<String, Set<String>> queryParams;
	private QueryParamsParser parser = new JsonApiQueryParamsParser();

	@Before
	public void prepare() {
		queryParams = new HashMap<>();
	}

	@Test
	public void onGivenSortingParserShouldReturnOnlyRequestParamsWithSorting() {
		// GIVEN
		queryParams.put("sort", Collections.singleton("name,-id"));
		queryParams.put("random", Collections.singleton("name,-id"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getSorting().getParams()).containsOnlyKeys(TestResource.class.getSimpleName());
		assertThat(result.getSorting().getParams().get(TestResource.class.getSimpleName()).getParams().size()).isEqualTo(2);
		assertThat(result.getSorting().getParams().get(TestResource.class.getSimpleName()).getParams().get("name")).isEqualTo(RestrictedSortingValues.asc);
		assertThat(result.getSorting().getParams().get(TestResource.class.getSimpleName()).getParams().get("id")).isEqualTo(RestrictedSortingValues.desc);
	}

	@Test
	public void onGivenIncludedFieldsParserShouldReturnOnlyRequestParamsWithIncludedFields() {
		// GIVEN
		queryParams.put("fields[users]", Collections.singleton("name,id"));
		queryParams.put("random[users]", Collections.singleton("surname"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getIncludedFields().getParams().size()).isEqualTo(1);
		assertThat(result.getIncludedFields().getParams().get("users").getParams()).containsOnly("name", "id");
	}

	@Test
	public void onGivenIncludedRelationsParserShouldReturnOnlyRequestParamsWithIncludedRelations() {
		// GIVEN
		queryParams.put("include", Collections.singleton("author,comments.author"));
		queryParams.put("random", Collections.singleton("author"));

		// WHEN
		QueryParams result = parseQueryParams();

		// THEN
		assertThat(result.getIncludedRelations().getParams().size()).isEqualTo(1);
		assertThat(result.getIncludedRelations().getParams().get(TestResource.class.getSimpleName()).getParams()).containsOnly(new Inclusion("author"), new Inclusion("comments.author"));
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
