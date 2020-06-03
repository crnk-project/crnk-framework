package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;

class ResourcePatchRelationshipsTest extends MetaResourceBaseTest {

	@Test
	void isUpdatable() {
		final Schema relationshipsSchema = checkRelationshipsSchema(true);
		assertIterableEquals(singleton("resourceRelation"), relationshipsSchema.getProperties().keySet());

		Schema resourceRelationSchema = ((ObjectSchema) relationshipsSchema).getProperties().get("resourceRelation");
		assertNotNull(resourceRelationSchema);
		assertEquals(ObjectSchema.class, resourceRelationSchema.getClass());
		Map<String, Schema> resourceRelationProps = ((ObjectSchema) resourceRelationSchema).getProperties();
		assertIterableEquals(singleton("data"), resourceRelationProps.keySet());

		Schema dataSchema = resourceRelationProps.get("data");
		assertNotNull(dataSchema);
		assertEquals(ComposedSchema.class, dataSchema.getClass());
		List<Schema> dataOneOfSchema = ((ComposedSchema) dataSchema).getOneOf();
		assertNotNull(dataOneOfSchema);
		assertEquals(2, dataOneOfSchema.size());
		assertEquals("#/components/schemas/RelatedResourceTypeResourceReference", dataOneOfSchema.get(1).get$ref());
	}

	@Test
	void notUpdatable() {
		final Schema relationshipsSchema = checkRelationshipsSchema(false);
		assertEquals(0, relationshipsSchema.getProperties().size());
	}

	private Schema checkRelationshipsSchema(boolean updatable) {
		relationshipMetaResourceField.setUpdatable(updatable);
		Schema schema = new ResourcePatchRelationships(metaResource).schema();

		assertNotNull(schema);
		assertEquals(ObjectSchema.class, schema.getClass());
		assertIterableEquals(singleton("relationships"), schema.getProperties().keySet());

		Schema relationshipsSchema = ((ObjectSchema) schema).getProperties().get("relationships");
		assertNotNull(relationshipsSchema);
		assertEquals(ObjectSchema.class, relationshipsSchema.getClass());

		return relationshipsSchema;
	}
}