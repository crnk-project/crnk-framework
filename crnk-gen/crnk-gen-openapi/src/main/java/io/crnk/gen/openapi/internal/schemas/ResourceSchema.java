package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

public class ResourceSchema extends AbstractSchemaGenerator {

	public ResourceSchema(MetaResource metaResource) {
		super(metaResource);
	}

	public Schema schema() {
		//Defines a schema for a JSON-API resource, without the enclosing top-level document.
		return new ComposedSchema().allOf(
				OASUtils.getIncludedSchemaRefs(
						new ResourceReference(metaResource),
						new ResourceAttributes(metaResource),
						new ResourceRelationships(metaResource),
						new ResourceLinks(metaResource)
				)).addRequiredItem("attributes");
	}
}
