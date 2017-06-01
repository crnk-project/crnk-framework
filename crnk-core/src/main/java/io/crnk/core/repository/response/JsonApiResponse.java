package io.crnk.core.repository.response;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

/**
 * This class will be deprecated in the future.
 */
public class JsonApiResponse {

	private Object entity;

	private MetaInformation metaInformation;

	private LinksInformation linksInformation;

	private Iterable<ErrorData> errors;

	public JsonApiResponse() {
	}

	public Object getEntity() {
		return entity;
	}

	public JsonApiResponse setEntity(Object entity) {
		this.entity = entity;
		return this;
	}

	public MetaInformation getMetaInformation() {
		return metaInformation;
	}

	public JsonApiResponse setMetaInformation(MetaInformation metaInformation) {
		this.metaInformation = metaInformation;
		return this;
	}

	public LinksInformation getLinksInformation() {
		return linksInformation;
	}

	public JsonApiResponse setLinksInformation(LinksInformation linksInformation) {
		this.linksInformation = linksInformation;
		return this;
	}

	public Iterable<ErrorData> getErrors() {
		return errors;
	}

	public JsonApiResponse setErrors(Iterable<ErrorData> errors) {
		this.errors = errors;
		return this;
	}
}
