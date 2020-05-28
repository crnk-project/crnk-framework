package io.crnk.core.engine.internal.document.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.repository.response.JsonApiResponse;
import org.junit.Before;

public abstract class AbstractDocumentMapperTest {

    protected DocumentMapper mapper;

    protected ObjectMapper objectMapper;

    protected ResourceFilterDirectory resourceFilterDirectory;

    protected DocumentMappingConfig mappingConfig;

    protected CoreTestContainer container;


    @Before
    public void setup() {
        mappingConfig = new DocumentMappingConfig();


        container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.getBoot().setPropertiesProvider(getPropertiesProvider());
        container.getBoot().getModuleRegistry().addPagingBehavior(new OffsetLimitPagingBehavior());
        container.boot();

        objectMapper = container.getBoot().getObjectMapper();
		objectMapper.findAndRegisterModules();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mapper = container.getBoot().getDocumentMapper();
        resourceFilterDirectory = container.getBoot().getModuleRegistry().getContext().getResourceFilterDirectory();
    }

    protected PropertiesProvider getPropertiesProvider() {
        return new NullPropertiesProvider();
    }

    protected QueryAdapter createAdapter(Class resourceClass) {
        ResourceRegistry resourceRegistry = container.getBoot().getResourceRegistry();
        ModuleRegistry moduleRegistry = container.getModuleRegistry();
        QueryContext queryContext = container.getQueryContext();
        return new QuerySpecAdapter(new QuerySpec(resourceClass), resourceRegistry, queryContext);
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
    	if (link.isTextual()) {
    		return link.asText();
		} else {
    		return link.get("href").asText();
		}
    }

}
