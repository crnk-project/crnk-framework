package io.crnk.gen.openapi.internal.annotations;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.annotation.Annotation;


public class OASAnnotations {

  public static Annotation[] resolve(MetaResource metaResource, MetaAttribute metaAttribute) {
    Annotation[] annotations;

    try {
      annotations = metaResource.getImplementationClass().getDeclaredField(
          metaAttribute.getUnderlyingName()
      ).getAnnotations();
    } catch (NoSuchFieldException e) {
      annotations = new Annotation[0];
    }
    return annotations;
  }

  public static Schema applySchemaAnnotations(Schema schema, Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof io.swagger.v3.oas.annotations.media.Schema) {
        io.swagger.v3.oas.annotations.media.Schema schemaAnnotation = (io.swagger.v3.oas.annotations.media.Schema) annotation;
        if (AnnotationsUtils.hasSchemaAnnotation(schemaAnnotation)) {
          schema.setDescription((schemaAnnotation).description());
        }
      }
    }
    return schema;
  }
}
