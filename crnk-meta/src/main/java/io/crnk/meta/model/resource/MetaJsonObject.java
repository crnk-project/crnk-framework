package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaDataObject;

@JsonApiResource(type = "metaJsonObject", resourcePath = "meta/jsonObject")
public class MetaJsonObject extends MetaDataObject {

}
