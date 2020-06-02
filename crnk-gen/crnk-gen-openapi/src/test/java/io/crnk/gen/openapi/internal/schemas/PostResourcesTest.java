package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PostResourcesTest extends MetaResourceBaseTest {

	@Test
	void schema() {
		Schema requestSchema = new PostResources(metaResource).schema();

		assertNotNull(requestSchema);
		assertEquals(ObjectSchema.class, requestSchema.getClass());
		ObjectSchema topLevelSchema = (ObjectSchema) requestSchema;
		assertIterableEquals(singleton("data"), topLevelSchema.getProperties().keySet());

		Schema dataSchema = topLevelSchema.getProperties().get("data");
		assertEquals(ArraySchema.class, dataSchema.getClass());
		Schema itemSchema = ((ArraySchema) dataSchema).getItems();
		assertNotNull(itemSchema);
		assertEquals("#/components/schemas/ResourceTypePostResourceData", itemSchema.get$ref());
	}
}
