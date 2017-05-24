package io.crnk.core.resource.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JsonApiModuleBuilder;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractDocumentMapperTest {

	protected DocumentMapper mapper;

	protected ResourceRegistry resourceRegistry;

	protected ObjectMapper objectMapper;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		ResourceInformationBuilder resourceInformationBuilder =
				new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistryBuilder registryBuilder =
				new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry,
				new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));

		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build());

		mapper = new DocumentMapper(resourceRegistry, objectMapper, getPropertiesProvider());
	}

	protected PropertiesProvider getPropertiesProvider() {
		return null;
	}

	@After
	public void tearDown() {
		MockRepositoryUtil.clear();
	}

	protected QueryAdapter createAdapter() {
		return new QueryParamsAdapter(new QueryParams());
	}

	protected QueryAdapter toAdapter(QuerySpec querySpec) {
		return new QuerySpecAdapter(querySpec, resourceRegistry);
	}

	protected JsonApiResponse toResponse(Object entity) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(entity);
		return response;
	}

}
