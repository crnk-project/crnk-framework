package io.crnk.core.engine.internal.document.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractDocumentMapperTest {

	protected DocumentMapper mapper;

	protected ObjectMapper objectMapper;

	protected ResourceFilterDirectory resourceFilterDirectory;

	protected DocumentMappingConfig mappingConfig;

	protected CoreTestContainer container;


	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		mappingConfig = new DocumentMappingConfig();


		container = new CoreTestContainer();
		container.setPackage(MockConstants.TEST_MODELS_PACKAGE);
		container.getBoot().setPropertiesProvider(getPropertiesProvider());
		container.getBoot().getModuleRegistry().addPagingBehavior(new OffsetLimitPagingBehavior());
		container.boot();

		objectMapper = container.getBoot().getObjectMapper();
		mapper = container.getBoot().getDocumentMapper();
		resourceFilterDirectory = container.getBoot().getModuleRegistry().getContext().getResourceFilterDirectory();
	}

	protected PropertiesProvider getPropertiesProvider() {
		return new NullPropertiesProvider();
	}

	@After
	public void tearDown() {
		MockRepositoryUtil.clear();
	}

	protected QueryAdapter createAdapter(Class resourceClass) {
		ResourceRegistry resourceRegistry = container.getBoot().getResourceRegistry();
		ModuleRegistry moduleRegistry = container.getModuleRegistry();
		QueryContext queryContext = container.getQueryContext();
		return new QueryParamsAdapter(resourceRegistry.getEntry(resourceClass).getResourceInformation(), new QueryParams(),
				moduleRegistry, queryContext);
	}

	protected QueryAdapter toAdapter(QuerySpec querySpec) {
		return container.toQueryAdapter(querySpec);
	}

	protected JsonApiResponse toResponse(Object entity) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(entity);
		return response;
	}

	protected String getLinkText(JsonNode link) {
		return link.asText();
	}

}
