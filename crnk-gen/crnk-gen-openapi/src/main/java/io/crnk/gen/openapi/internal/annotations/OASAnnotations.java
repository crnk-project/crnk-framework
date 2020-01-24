package io.crnk.gen.openapi.internal.annotations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.gen.openapi.internal.OASGenerator;
import io.crnk.gen.openapi.internal.OASMergeUtil;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
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
    // Hack to support ObjectNode: When an Object has two setters with the same number
    // of parameters the parser blows up.
    ObjectMapper mapper = Json.mapper();
    mapper.addMixIn(ObjectNode.class, IgnoreObjectNodeSetAllIntMixIn.class);
  }

  private static class SingletonHelper {
    private static final OASAnnotations INSTANCE = new OASAnnotations();
  }

  abstract static class IgnoreObjectNodeSetAllIntMixIn
  {
    @JsonIgnore
    public abstract void setAll(ObjectNode other);
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
        LOGGER.warn("Unable to parse model" + metaResource.getName());
        return;
      }
    }

    String[] words = metaResource.getImplementationClassName().split(Pattern.quote("."));
    Schema derivedSchema = model.get(words[words.length - 1]);
    derivedSchema = (Schema) derivedSchema.getProperties().get(metaAttribute.getUnderlyingName());
    OASMergeUtil.mergeSchema(schema, derivedSchema);
  }
}
