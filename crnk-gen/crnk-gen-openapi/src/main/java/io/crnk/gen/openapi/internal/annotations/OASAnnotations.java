package io.crnk.gen.openapi.internal.annotations;

import io.crnk.gen.openapi.internal.OASGenerator;
import io.crnk.gen.openapi.internal.OASMergeUtil;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class OASAnnotations {

  private static final Logger LOGGER = LoggerFactory.getLogger(OASGenerator.class);

  private static Map<Type, Map<String, Schema>> cache;
  private static Set<Type> failed;

  private OASAnnotations() {
    cache = new HashMap<>();
    failed = new HashSet<>();
  }

  private static class SingletonHelper {
    private static final OASAnnotations INSTANCE = new OASAnnotations();
  }

  public static OASAnnotations getInstance() {
    return SingletonHelper.INSTANCE;
  }

  public void applyFromModel(Schema schema, MetaResource metaResource, MetaAttribute metaAttribute) {
    Type type = metaResource.getImplementationType();

    if (failed.contains(type)) {
      return;
    }

    Map<String, Schema> model = cache.get(type);

    if (model == null) {
      try {
        model = ModelConverters.getInstance().read(type);
        cache.put(type, model);
      } catch (Exception e) {
        failed.add(type);
        LOGGER.error("Unable to parse model" + metaResource.getName(), e);
        return;
      }
    }

    String[] words = metaResource.getImplementationClassName().split(Pattern.quote("."));
    Schema derivedSchema = model.get(words[words.length - 1]);
    derivedSchema = (Schema) derivedSchema.getProperties().get(metaAttribute.getUnderlyingName());
    OASMergeUtil.mergeSchema(schema, derivedSchema);
  }
}
