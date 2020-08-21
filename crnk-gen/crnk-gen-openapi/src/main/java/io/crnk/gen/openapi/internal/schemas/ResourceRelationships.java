package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import static java.util.stream.Collectors.toMap;

public class ResourceRelationships extends AbstractResourceAttributes {

	public ResourceRelationships(MetaResource metaResource) {
		super(metaResource, "relationships",
				OASUtils.associationAttributes(metaResource, false)
				.collect(toMap(
						MetaResourceField::getName,
						e -> generateRelationshipSchema(metaResource, e))));
	}

	private static Schema generateRelationshipSchema(MetaResource metaResource, MetaResourceField field) {
		return new ObjectSchema()
				.addProperties("links", new ObjectSchema()
						.addProperties("self", new StringSchema()
								._default(OASUtils.getRelationshipsPath(metaResource, field)))
						.addProperties("related", new StringSchema()
								._default(OASUtils.getNestedPath(metaResource, field))))
				.addProperties("data", new ComposedSchema()
						.addOneOfItem(new ArraySchema().items(OASUtils.transformMetaResourceField(field.getType())))
						.addOneOfItem(OASUtils.transformMetaResourceField(field.getType()))
				);
	}

	@Override
	public Schema schema() {
		return super.schema()
				.description("Relationships between the resource and other JSON:API resources");
	}
}