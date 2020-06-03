package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;

import java.util.stream.Collectors;

import static io.crnk.gen.openapi.internal.OASUtils.notAssociationAttributes;
import static io.crnk.gen.openapi.internal.OASUtils.patchAttributes;

public class ResourcePatchAttributes extends AbstractResourceAttributes {

	public ResourcePatchAttributes(MetaResource metaResource) {
		super(metaResource, "attributes",
				notAssociationAttributes(patchAttributes(metaResource, false))
						.collect(
								Collectors.toMap(
										MetaElement::getName,
										e -> new ResourceAttribute(metaResource, e).$ref())));
	}
}
