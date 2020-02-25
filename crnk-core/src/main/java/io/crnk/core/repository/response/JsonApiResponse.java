package io.crnk.core.repository.response;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will be deprecated in the future.
 */
public class JsonApiResponse {

	private Object entity;

	private MetaInformation metaInformation;

	private LinksInformation linksInformation;

	private List<ErrorData> errors;

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

	public List<ErrorData> getErrors() {
		return errors;
	}

	public JsonApiResponse setErrors(Iterable<ErrorData> errors) {
		this.errors = new ArrayList<>();
		for (ErrorData error : errors) {
			this.errors.add(error);
		}
		return this;
	}

	public JsonApiResponse setErrors(List<ErrorData> errors) {
		this.errors = errors;
		return this;
	}

	@Override
	public String toString() {
		return "JsonApiResponse[data=" + entity + ", errors=" + errors + "]";
	}
}
