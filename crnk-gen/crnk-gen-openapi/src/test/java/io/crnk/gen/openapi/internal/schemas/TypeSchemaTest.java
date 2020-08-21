package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class TypeSchemaTest extends MetaResourceBaseTest {

	@Test
	void schema() {
		final Schema typeSchema = new TypeSchema(metaResource).schema();
		assertNotNull(typeSchema);
		assertEquals(StringSchema.class, typeSchema.getClass());
		assertIterableEquals(singletonList("ResourceType"), typeSchema.getEnum());
	}
}