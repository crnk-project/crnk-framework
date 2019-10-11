package io.crnk.gen.openapi.internal.annotations;

import io.crnk.gen.openapi.internal.OASGenerator;
import io.crnk.gen.openapi.internal.OASMergeUtil;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;


public class OASAnnotations {

  private static final Logger LOGGER = LoggerFactory.getLogger(OASGenerator.class);

  public static Schema applyFromModel(Schema schema, MetaResource metaResource, MetaAttribute metaAttribute) {
    Map<String, Schema> model;

    try {
      model = ModelConverters.getInstance().read(metaResource.getImplementationType());
    } catch (Exception e) {
      LOGGER.error("Unable to parse model" + metaResource.getName(), e);
      return schema;
    }

    String[] words = metaResource.getImplementationClassName().split(Pattern.quote("."));
    Schema derivedSchema = model.get(words[words.length - 1]);
    derivedSchema = (Schema) derivedSchema.getProperties().get(metaAttribute.getUnderlyingName());
    return OASMergeUtil.mergeSchema(schema, derivedSchema);
  }

  public static Operation applyFromModel(Operation operation, MetaResource metaResource, MetaAttribute metaAttribute) {
    Map<String, Schema> model;

    try {
      model = ModelConverters.getInstance().read(metaResource.getImplementationType());
    } catch (Exception e) {
      LOGGER.error("Unable to parse model" + metaResource.getName(), e);
      return operation;
    }

//    String[] words = metaResource.getImplementationClassName().split(Pattern.quote("."));
//    Schema derivedSchema = model.get(words[words.length - 1]);
//    derivedSchema = (Schema) derivedSchema.getProperties().get(metaAttribute.getUnderlyingName());
    return OASMergeUtil.mergeOperations(operation, operation);
  }
}
