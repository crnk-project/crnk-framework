package io.crnk.core.engine.internal.http;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.FieldPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;

import java.util.Map;
import java.util.Set;

public class JsonApiRequestProcessorHelper {

    private Module.ModuleContext moduleContext;

    public JsonApiRequestProcessorHelper(Module.ModuleContext moduleContext) {
        this.moduleContext = moduleContext;
    }

    public JsonPath getJsonPath(HttpRequestContext requestContext) {
        ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
        TypeParser typeParser = moduleContext.getTypeParser();

        PathBuilder pathBuilder = new PathBuilder(resourceRegistry, typeParser);
        String path = requestContext.getPath();
        QueryContext queryContext = requestContext.getQueryContext();
        return pathBuilder.build(path, queryContext);
    }

    public QueryAdapter toQueryAdapter(Map<String, Set<String>> parameters, JsonPath jsonPath, QueryContext queryContext) {
        ResourceInformation resourceInformation = getRequestedResource(jsonPath);
        QueryAdapterBuilder queryAdapterBuilder = moduleContext.getModuleRegistry().getQueryAdapterBuilder();
        return queryAdapterBuilder.build(resourceInformation, parameters, queryContext);
    }


    protected ResourceInformation getRequestedResource(JsonPath jsonPath) {
        ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
        RegistryEntry registryEntry = jsonPath.getRootEntry();

        ResourceField field = (jsonPath instanceof RelationshipsPath) ? ((RelationshipsPath) jsonPath).getRelationship()
                : jsonPath instanceof FieldPath ? ((FieldPath) jsonPath).getField() : null;
        if (field != null) {
            String oppositeResourceType = field.getOppositeResourceType();
            return resourceRegistry.getEntry(oppositeResourceType).getResourceInformation();
        } else {
            return registryEntry.getResourceInformation();
        }
    }

}
