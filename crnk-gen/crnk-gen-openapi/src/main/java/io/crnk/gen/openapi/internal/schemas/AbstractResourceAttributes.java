package io.crnk.gen.openapi.internal.schemas;

import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

abstract class AbstractResourceAttributes extends AbstractSchemaGenerator {

	private final Map<String, Schema> schemaProperties;
	private final String topLevelName;

	public AbstractResourceAttributes(MetaResource metaResource, String topLevelName, Map<String, Schema> schemaProperties) {
		super(metaResource);
		this.schemaProperties = notNull(schemaProperties, "schemaProperties cannot be null");
		this.topLevelName = notEmpty(topLevelName, "topLevelName cannot be empty");
	}

	@Override
	public Schema schema() {
		return new ObjectSchema().addProperties(topLevelName, new ObjectSchema().properties(schemaProperties));
	}

	/**
	 * @return if this schema is included in the final OAS spec
	 */
	@Override
	public boolean isIncluded() {
		return schemaProperties.size() > 0;
	}
}