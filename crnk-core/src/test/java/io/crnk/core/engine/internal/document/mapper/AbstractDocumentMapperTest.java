package io.crnk.core.engine.internal.document.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractDocumentMapperTest {

	protected DocumentMapper mapper;

	protected ResourceRegistry resourceRegistry;

	protected ObjectMapper objectMapper;

	protected ResourceFilterDirectory resourceFilterDirectory;
	private ModuleRegistry moduleRegistry;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		boot.setPropertiesProvider(getPropertiesProvider());
		boot.boot();

		objectMapper = boot.getObjectMapper();
		moduleRegistry = boot.getModuleRegistry();
		mapper = boot.getDocumentMapper();
		resourceRegistry = boot.getResourceRegistry();
		resourceFilterDirectory = boot.getModuleRegistry().getContext().getResourceFilterDirectory();
	}

	protected PropertiesProvider getPropertiesProvider() {
		return new NullPropertiesProvider();
	}

	@After
	public void tearDown() {
		MockRepositoryUtil.clear();
	}

	protected QueryAdapter createAdapter(Class resourceClass) {
		return new QueryParamsAdapter(resourceRegistry.getEntry(resourceClass).getResourceInformation(), new QueryParams(), moduleRegistry);
	}

	protected QueryAdapter toAdapter(QuerySpec querySpec) {
		return new QuerySpecAdapter(querySpec, resourceRegistry);
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
