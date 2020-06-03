package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourceSchemaTest extends MetaResourceBaseTest {

	@Test
	void schema() {
		Schema resourceSchema = new ResourceSchema(metaResource).schema();
		assertNotNull(resourceSchema);
		assertEquals(ComposedSchema.class, resourceSchema.getClass());
		assertIterableEquals(singletonList("attributes"), resourceSchema.getRequired());

		List<Schema> allOf = ((ComposedSchema) resourceSchema).getAllOf();
		assertNotNull(allOf);
		List<String> allOfItems = allOf.stream().map(Schema::get$ref).collect(toList());
		assertIterableEquals(Stream.of(
				"#/components/schemas/ResourceTypeResourceReference",
				"#/components/schemas/ResourceTypeResourceAttributes",
				"#/components/schemas/ResourceTypeResourceRelationships",
				"#/components/schemas/ResourceTypeResourceLinks"
		).collect(toList()), allOfItems);
	}
}
