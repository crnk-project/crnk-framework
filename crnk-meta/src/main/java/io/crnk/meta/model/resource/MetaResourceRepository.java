package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;

@JsonApiResource(type = "meta/resourceRepository")
public class MetaResourceRepository extends MetaElement {

	@JsonApiRelation(serialize = SerializeType.LAZY)
	private MetaResource resourceType;

	@JsonApiRelation(serialize = SerializeType.LAZY)
	private MetaDataObject listMetaType;

	@JsonApiRelation(serialize = SerializeType.LAZY)
	private MetaDataObject listLinksType;

	private boolean exposed;

	public boolean isExposed() {
		return exposed;
	}

	public void setExposed(boolean exposed) {
		this.exposed = exposed;
	}

	public MetaResource getResourceType() {
		return resourceType;
	}

	public void setResourceType(MetaResource resourceType) {
		this.resourceType = resourceType;
	}

	public MetaDataObject getListMetaType() {
		return listMetaType;
	}

	public void setListMetaType(MetaDataObject listMetaType) {
		this.listMetaType = listMetaType;
	}

	public MetaDataObject getListLinksType() {
		return listLinksType;
	}

	public void setListLinksType(MetaDataObject listLinksType) {
		this.listLinksType = listLinksType;
	}
}
