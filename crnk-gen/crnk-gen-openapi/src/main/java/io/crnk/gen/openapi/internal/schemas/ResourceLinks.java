package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class ResourceLinks extends AbstractSchemaGenerator {

	public ResourceLinks(MetaResource metaResource) {
		super(metaResource);
	}

	@Override
	public Schema schema() {
		return new ObjectSchema()
				.description("Links related to the resource")
				.addProperties("links",
						new ObjectSchema()
								.addProperties("self", new StringSchema()
										._default(OASUtils.getResourcePath(metaResource))
										.format("uri"))
								.additionalProperties(new Link().$ref()));
	}
}