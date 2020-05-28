package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ResourceAttributeTest extends MetaResourceBaseTest {

	@Test
	void schemaPrimaryKey() {
		MetaResource metaResource = getTestMetaResource();
		MetaResourceField metaResourceField = (MetaResourceField) metaResource.getChildren().get(0);

		Schema schema = new ResourceAttribute(metaResource, metaResourceField).schema();
		Assert.assertTrue(schema instanceof StringSchema);
		Assert.assertEquals("The JSON:API resource ID", schema.getDescription());
	}

	@Test
	void schema() {
		MetaResource metaResource = getTestMetaResource();
		MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);

		Schema schema = new ResourceAttribute(metaResource, additionalMetaResourceField).schema();
		Assert.assertTrue(schema instanceof StringSchema);
		Assert.assertNull(schema.getDescription());
	}

	@Test
	void schemaNullable() {
		MetaResource metaResource = getTestMetaResource();
		MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);
		additionalMetaResourceField.setNullable(true);

		Schema schema = new ResourceAttribute(metaResource, additionalMetaResourceField).schema();
		Assert.assertTrue(schema instanceof StringSchema);
		Assert.assertTrue(schema.getNullable());
	}

	@Test
	@Disabled
	void notNullable() {
		MetaResource metaResource = getTestMetaResource();
		MetaResourceField additionalMetaResourceField = (MetaResourceField) metaResource.getChildren().get(1);
		additionalMetaResourceField.setNullable(false);

		Schema schema = new ResourceAttribute(metaResource, additionalMetaResourceField).schema();
		Assert.assertTrue(schema instanceof StringSchema);
		Assert.assertFalse(schema.getNullable());
	}
}
