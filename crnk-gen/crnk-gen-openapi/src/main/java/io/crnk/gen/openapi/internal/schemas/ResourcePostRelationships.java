package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;

import static io.crnk.gen.openapi.internal.OASUtils.associationAttributes;
import static io.crnk.gen.openapi.internal.OASUtils.postAttributes;
import static java.util.stream.Collectors.toMap;

public class ResourcePostRelationships extends AbstractResourceAttributes {

	public ResourcePostRelationships(MetaResource metaResource) {
		super(metaResource, "relationships",
				associationAttributes(postAttributes(metaResource, false))
						.collect(toMap(
								MetaElement::getName,
								e -> new ObjectSchema().addProperties("data", new ComposedSchema()
										.addOneOfItem(new ArraySchema().items(OASUtils.transformMetaResourceField(e.getType())))
										.addOneOfItem(OASUtils.transformMetaResourceField(e.getType())))
						)));
	}
}