package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class PatchResource extends AbstractSchemaGenerator {

	public PatchResource(MetaResource metaResource) {
		super(metaResource);
	}

	@Override
	public Schema schema() {
		return new ObjectSchema()
				.addRequiredItem("data")
				.addProperties("data", new ComposedSchema()
						.addAllOfItem(new ResourceReference(metaResource).$ref())
						.addAllOfItem(new ResourcePatchAttributes(metaResource).$ref()));
	}
}
