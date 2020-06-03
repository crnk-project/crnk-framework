package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;

import static io.crnk.gen.openapi.internal.OASUtils.notAssociationAttributes;
import static io.crnk.gen.openapi.internal.OASUtils.postAttributes;
import static java.util.stream.Collectors.toMap;

public class ResourcePostAttributes extends AbstractResourceAttributes {

	public ResourcePostAttributes(MetaResource metaResource) {
		super(metaResource, "attributes",
				notAssociationAttributes(postAttributes(metaResource, false))
						.collect(toMap(
								MetaElement::getName,
								e -> new ResourceAttribute(metaResource, e).$ref())));
	}
}
