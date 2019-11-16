package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;

public class PostResource extends AbstractSchemaGenerator {

  public PostResource(MetaResource metaResource) {
    super(metaResource);
  }

  public Schema schema() {
    return new ComposedSchema()
        // TODO: One of, or a list of depending on metaResource.getRespository().isBulk()
        .allOf(
            Arrays.asList(
                new PostResourceReference(metaResource).$ref(),
                new ResourcePostAttributes(metaResource).$ref()));
  }
}
