package io.crnk.gen.openapi.internal;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OASMergeUtil {

  @SafeVarargs
  public static Map<String, ApiResponse> mergeApiResponses(Map<String, ApiResponse>... maps) {
    Map<String, ApiResponse> merged = new TreeMap<>();
    for (Map<String, ApiResponse> map : maps) {
      merged.putAll(map);
    }
    return merged;
  }

  public static Operation mergeOperations(Operation thisOperation, Operation thatOperation) {
    if (thatOperation == null) {
      return thisOperation;
    }

    if (thatOperation.getTags() != null) {
      thisOperation.setTags(
          mergeTags(thisOperation.getTags(), thatOperation.getTags())
      );
    }
    if (thatOperation.getExternalDocs() != null) {
      thisOperation.setExternalDocs(
        mergeExternalDocumentation(thisOperation.getExternalDocs(), thatOperation.getExternalDocs())
      );
    }
    if (thatOperation.getParameters() != null) {
      thisOperation.setParameters(
          mergeParameters(thisOperation.getParameters(), thatOperation.getParameters())
      );
    }
    if (thatOperation.getRequestBody() != null) {
      thisOperation.setRequestBody(thatOperation.getRequestBody());
    }
    if (thatOperation.getResponses() != null) {
      thisOperation.setResponses(thatOperation.getResponses());
    }
    if (thatOperation.getCallbacks() != null) {
      thisOperation.setCallbacks(thatOperation.getCallbacks());
    }
    if (thatOperation.getDeprecated() != null) {
      thisOperation.setDeprecated(thatOperation.getDeprecated());
    }
    if (thatOperation.getSecurity() != null) {
      thisOperation.setSecurity(thatOperation.getSecurity());
    }
    if (thatOperation.getServers() != null) {
      thisOperation.setServers(thatOperation.getServers());
    }
    if (thatOperation.getExtensions() != null) {
      thisOperation.setExtensions(thatOperation.getExtensions());
    }
    if (thatOperation.getOperationId() != null) {
      thisOperation.setOperationId(thatOperation.getOperationId());
    }
    if (thatOperation.getSummary() != null) {
      thisOperation.setSummary(thatOperation.getSummary());
    }
    if (thatOperation.getDescription() != null) {
      thisOperation.setDescription(thatOperation.getDescription());
    }
    if (thatOperation.getExtensions() != null) {
      thisOperation.setExtensions(thatOperation.getExtensions());
    }
    return thisOperation;
  }

  static ExternalDocumentation mergeExternalDocumentation(ExternalDocumentation thisExternalDocumentation,
                                                          ExternalDocumentation thatExternalDocumentation) {
    if (thatExternalDocumentation == null) {
      return thisExternalDocumentation;
    }
    if (thisExternalDocumentation == null) {
      return thatExternalDocumentation;
    }
    if (thatExternalDocumentation.getDescription() != null) {
      thisExternalDocumentation.setDescription(thatExternalDocumentation.getDescription());
    }
    if (thatExternalDocumentation.getUrl() != null) {
      thisExternalDocumentation.setUrl(thatExternalDocumentation.getUrl());
    }
    if (thatExternalDocumentation.getExtensions() != null) {
      thisExternalDocumentation.setExtensions(thatExternalDocumentation.getExtensions());
    }
    return thisExternalDocumentation;
  }

  static List<String> mergeTags(List<String> thisTags, List<String> thatTags) {
    if (thisTags == null) {
      return thatTags;
    }
    return Stream.concat(thisTags.stream(), thatTags.stream())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  static List<Parameter> mergeParameters(List<Parameter> thisParameters, List<Parameter> thatParameters) {
    Map<String, Parameter> thisParametersMap = new TreeMap();

    for (Parameter thisParameter : thisParameters) {
      // Only parameters that have matching `name` and `in` fields are deep merged
      thisParametersMap.put(thisParameter.getName() + ":" + thisParameter.getIn(), thisParameter);
    }

    Parameter thisParameter;
    for (Parameter thatParameter : thatParameters) {
      thisParameter = thisParametersMap.get(thatParameter.getName() + ":" + thatParameter.getIn());
      if (thisParameter == null) {
        thisParameters.add(thatParameter);
      } else {
        mergeParameter(thisParameter, thatParameter);
      }
    }

    return thisParameters;
  }

  public static Parameter mergeParameter(Parameter thisParameter, Parameter thatParameter) {
    if (thatParameter.getName() != null) {
      thisParameter.setName(thatParameter.getName());
    }
    if (thatParameter.getIn() != null) {
      thisParameter.setIn(thatParameter.getIn());
    }
    if (thatParameter.getDescription() != null) {
      thisParameter.setDescription(thatParameter.getDescription());
    }
    if (thatParameter.getRequired() != null) {
      thisParameter.setRequired(thatParameter.getRequired());
    }
    if (thatParameter.getDeprecated() != null) {
      thisParameter.setDeprecated(thatParameter.getDeprecated());
    }
    if (thatParameter.getAllowEmptyValue() != null) {
      thisParameter.setAllowEmptyValue(thatParameter.getAllowEmptyValue());
    }
    if (thatParameter.get$ref() != null) {
      thisParameter.set$ref(thatParameter.get$ref());
    }
    return thisParameter;
  }
  
  public static Schema mergeSchema(Schema thisSchema, Schema thatSchema) {
    if (thatSchema == null) {
      return thisSchema;
    }
    // Overwriting `implementation` is explicitly disallowed
    // Overwriting `not` is explicitly disallowed
    // Overwriting `oneOf` is explicitly disallowed
    // Overwriting `anyOf` is explicitly disallowed
    // Overwriting `allOf` is explicitly disallowed
    // Overwriting `name` is explicitly disallowed
    if (thatSchema.getTitle() != null) {
      thisSchema.setTitle(thatSchema.getTitle());
    }
    // Overwriting `multipleOf` is explicitly disallowed
    if (thatSchema.getMaximum() != null) {
      thisSchema.setMaximum(thatSchema.getMaximum());
    }
    if (thatSchema.getExclusiveMaximum() != null) {
      thisSchema.setExclusiveMaximum(thatSchema.getExclusiveMaximum());
    }

    if (thatSchema.getMinimum() != null) {
      thisSchema.setMinimum(thatSchema.getMinimum());
    }
    if (thatSchema.getExclusiveMinimum() != null) {
      thisSchema.setExclusiveMinimum(thatSchema.getExclusiveMinimum());
    }
    if (thatSchema.getMaxLength() != null) {
      thisSchema.setMaxLength(thatSchema.getMaxLength());
    }
    if (thatSchema.getMinLength() != null) {
      thisSchema.setMinLength(thatSchema.getMinLength());
    }
    if (thatSchema.getPattern() != null) {
      thisSchema.setPattern(thatSchema.getPattern());
    }
    if (thatSchema.getMaxProperties() != null) {
      thisSchema.setMaxProperties(thatSchema.getMaxProperties());
    }
    if (thatSchema.getMinProperties() != null) {
      thisSchema.setMinProperties(thatSchema.getMinProperties());
    }
    // RequiredProperties
    if (thatSchema.getRequired() != null) {
      thisSchema.setRequired(thatSchema.getRequired());
    }
    // Overwriting `name` is explicitly disallowed
    if (thatSchema.getDescription() != null) {
      thisSchema.setDescription(thatSchema.getDescription());
    }
    if (thatSchema.getFormat() != null) {
      thisSchema.setFormat(thatSchema.getFormat());
    }
    // Overwriting `ref` is explicitly disallowed
    if (thatSchema.getNullable() != null) {
      thisSchema.setNullable(thatSchema.getNullable());
    }
    // Overwriting `AccessMode` is explicitly disallowed
    if (thatSchema.getExample() != null) {
      thisSchema.setExample(thatSchema.getExample());
    }
    if (thatSchema.getExternalDocs() != null) {
      thisSchema.setExternalDocs(thatSchema.getExternalDocs());
    }
    if (thatSchema.getDeprecated() != null) {
      thisSchema.setDeprecated(thatSchema.getDeprecated());
    }
    if (thatSchema.getType() != null) {
      thisSchema.setType(thatSchema.getType());
    }
    if (thatSchema.getEnum() != null) {
      thisSchema.setEnum(thatSchema.getEnum());
    }
    if (thatSchema.getDefault() != null) {
      thisSchema.setDefault(thatSchema.getDefault());
    }
    // Overwriting `discriminator` is explicitly disallowed
    // Overwriting `hidden` is explicitly disallowed
    // Overwriting `subTypes` is explicitly disallowed
    if (thatSchema.getExtensions() != null) {
      thisSchema.setExtensions(thatSchema.getExtensions());
    }
    return thisSchema;
  }
}
