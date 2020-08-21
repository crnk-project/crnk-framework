package io.crnk.gen.openapi.internal.schemas;

import io.crnk.gen.openapi.internal.MetaResourceBaseTest;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;

class ResourceRelationshipsTest extends MetaResourceBaseTest {

	@Test
	void schema() {
		Schema schema = new ResourceRelationships(metaResource).schema();

		assertNotNull(schema);
		assertEquals(ObjectSchema.class, schema.getClass());
		assertIterableEquals(singleton("relationships"), schema.getProperties().keySet());

		Schema relationshipsSchema = ((ObjectSchema) schema).getProperties().get("relationships");
		assertNotNull(relationshipsSchema);
		assertEquals(ObjectSchema.class, relationshipsSchema.getClass());
		assertIterableEquals(singleton("resourceRelation"), relationshipsSchema.getProperties().keySet());

		Schema resourceRelationSchema = ((ObjectSchema) relationshipsSchema).getProperties().get("resourceRelation");
		assertNotNull(resourceRelationSchema);
		assertEquals(ObjectSchema.class, resourceRelationSchema.getClass());
		Map<String, Schema> resourceRelationProps = ((ObjectSchema) resourceRelationSchema).getProperties();
		assertIterableEquals(Stream.of("data", "links").collect(Collectors.toSet()), new HashSet<>(resourceRelationProps.keySet()));

		Schema dataSchema = resourceRelationProps.get("data");
		assertNotNull(dataSchema);
		assertEquals(ComposedSchema.class, dataSchema.getClass());
		List<Schema> dataOneOfSchema = ((ComposedSchema) dataSchema).getOneOf();
		assertNotNull(dataOneOfSchema);
		assertEquals(2, dataOneOfSchema.size());
		assertEquals("#/components/schemas/RelatedResourceTypeResourceReference", dataOneOfSchema.get(1).get$ref());
	}
}