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

	/**
	 * @param jsonApiResponse Copy entity, metaInformation, linksInformation, errors from {@link JsonApiResponse}
	 */
	public JsonApiResponse(JsonApiResponse jsonApiResponse) {
		this.entity = jsonApiResponse.entity;
		this.metaInformation = jsonApiResponse.metaInformation;
		this.linksInformation = jsonApiResponse.linksInformation;
		this.errors = jsonApiResponse.errors;
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
