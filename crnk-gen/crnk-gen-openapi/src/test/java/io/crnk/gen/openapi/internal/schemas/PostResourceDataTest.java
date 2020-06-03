package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PostResourceDataTest extends MetaResourceBaseTest {

	@Test
	public void schema() {
		additionalMetaResourceField.setInsertable(true);
		final Schema dataSchema = new PostResourceData(metaResource).schema();

		assertNotNull(dataSchema);
		assertEquals(ComposedSchema.class, dataSchema.getClass());
		final List<Schema> allOf = ((ComposedSchema) dataSchema).getAllOf();
		assertNotNull(allOf);
		assertEquals(2, allOf.size());
		assertEquals(
				"#/components/schemas/ResourceTypePostResourceReference",
				allOf.get(0).get$ref(),
				"Post resource uses a special <Type>PostResourceReference in which the id is optional");
		assertEquals("#/components/schemas/ResourceTypeResourcePostAttributes", allOf.get(1).get$ref());
	}
}