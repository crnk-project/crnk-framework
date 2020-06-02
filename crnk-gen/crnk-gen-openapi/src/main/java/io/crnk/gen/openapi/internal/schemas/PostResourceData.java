package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

public class PostResourceData extends AbstractSchemaGenerator {

	public PostResourceData(MetaResource metaResource) {
		super(metaResource);
	}

	@Override
	public Schema schema() {
		return new ComposedSchema()
				.addAllOfItem(new PostResourceReference(metaResource).$ref())
				.addAllOfItem(new ResourcePostAttributes(metaResource).$ref());
	}
}
