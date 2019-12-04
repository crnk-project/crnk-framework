package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;


class PaginationTest {

  @Test
  void schema() {
    Schema schema = new Pagination().schema();
    Assert.assertTrue(schema instanceof ObjectSchema);

    Stream.of("first", "last", "prev", "next").forEach(
        key -> {
          Schema subSchema = (Schema) schema.getProperties().get(key);
          Assert.assertTrue(subSchema instanceof StringSchema);
          Assert.assertTrue(subSchema.getNullable());
          Assert.assertEquals("uri", subSchema.getFormat());
        }
    );
    Assert.assertEquals(4, schema.getProperties().size());
  }
}
