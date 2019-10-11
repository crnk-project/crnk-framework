package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.gen.openapi.internal.schemas.ResourceReferenceResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceReferencesResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

abstract class AbstractNestedMutateOperation extends AbstractNestedOperation {

  public static OperationType operationType;

  final Map<String, ApiResponse> responses;

  private final String prefix;

  AbstractNestedMutateOperation(MetaResource metaResource, MetaResourceField metaResourceField, MetaResource relatedMetaResource) {
    super(metaResource, metaResourceField, relatedMetaResource);
    prefix = "";
    responses = defaultResponsesMap();
  }


  Operation operation() {
    Operation operation = super.operation();
    operation.getParameters().add(new PrimaryKey(this.metaResource).$ref());
    Schema relationshipSchema = OASUtils.oneToMany(metaResourceField) ?
        new ResourceReferencesResponseSchema(relatedMetaResource).$ref() :
        new ResourceReferenceResponseSchema(relatedMetaResource).$ref();
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType("application/vnd.api+json",
                        new MediaType()
                            .schema(relationshipSchema))));
    return operation;
  }
}
