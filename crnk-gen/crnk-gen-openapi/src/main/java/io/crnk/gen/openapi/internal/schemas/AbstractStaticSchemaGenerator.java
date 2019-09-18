package io.crnk.gen.openapi.internal.schemas;

import io.swagger.v3.oas.models.media.Schema;

abstract class AbstractStaticSchemaGenerator {
  public static String getName() {
    return Thread.currentThread().getStackTrace()[1].getClass().getSimpleName();
  }
  public static Schema $ref() {
    return new Schema().$ref(getName());
  }
}
