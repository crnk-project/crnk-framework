package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class PatchResourceTest extends MetaResourceBaseTest {

	@Test
	void schema() {
		relationshipMetaResourceField.setUpdatable(true);
		Schema requestSchema = new PatchResource(metaResource).schema();

		ObjectSchema topLevelSchema = (ObjectSchema) requestSchema;
		assertIterableEquals(singleton("data"), topLevelSchema.getProperties().keySet());
		Schema dataSchema = topLevelSchema.getProperties().get("data");
		assertEquals(ComposedSchema.class, dataSchema.getClass());
		List<Schema> allOf = ((ComposedSchema) dataSchema).getAllOf();
		assertEquals(2, allOf.size());
		assertEquals("#/components/schemas/ResourceTypeResourceReference", allOf.get(0).get$ref());
		assertEquals("#/components/schemas/ResourceTypeResourcePatchRelationships", allOf.get(1).get$ref());
	}
}
