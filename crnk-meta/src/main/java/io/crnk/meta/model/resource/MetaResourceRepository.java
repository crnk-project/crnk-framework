package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;

@JsonApiResource("meta/resourceRepository")
public class MetaResourceRepository extends MetaElement {

	private MetaResource resourceType;

	private MetaDataObject listMetaType;

	private MetaDataObject listLinksType;

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
