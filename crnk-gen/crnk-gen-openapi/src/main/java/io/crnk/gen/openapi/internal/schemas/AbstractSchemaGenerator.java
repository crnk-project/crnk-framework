package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractSchemaGenerator {

	protected final MetaResource metaResource;

	protected final MetaAttribute metaAttribute;

	private final String prefix;

	protected AbstractSchemaGenerator() {
		this.metaResource = null;
		this.metaAttribute = null;
		prefix = "";
	}

	protected AbstractSchemaGenerator(MetaResource metaResource) {
		this.metaResource = metaResource;
		this.metaAttribute = null;
		prefix = StringUtils.capitalize(metaResource.getResourceType());
	}

	protected AbstractSchemaGenerator(MetaResource metaResource, MetaAttribute metaAttribute) {
		this.metaResource = metaResource;
		this.metaAttribute = metaAttribute;
		prefix = StringUtils.capitalize(metaResource.getResourceType()) + StringUtils.capitalize(metaAttribute.getName());
	}

	public String getName() {
		return prefix + getClass().getSimpleName();
	}

	public Schema $ref() {
		return new Schema().$ref(getName());
	}

	/**
	 * @return if this schema is included in the final OAS spec
	 */
	public boolean isIncluded() {
		return true;
	}

	abstract public Schema schema();
}
