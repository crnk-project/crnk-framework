package io.crnk.gen.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

public enum OutputFormat {
  JSON {
    @Override
    public String extension() {
      return this.name().toLowerCase();
    }

    @Override
    public ObjectMapper mapper() {
      return Json.mapper();
    }

    @Override
    public String pretty(OpenAPI openAPI) {
      return Json.pretty(openAPI);
    }
  },
  YAML {
    @Override
    public String extension() {
      return this.name().toLowerCase();
    }

    @Override
    public ObjectMapper mapper() {
      return Yaml.mapper();
    }

    @Override
    public String pretty(OpenAPI openAPI) {
      return Yaml.pretty(openAPI);
    }
  };

  public abstract String extension();

  public abstract ObjectMapper mapper();

  public abstract String pretty(OpenAPI openAPI);

}
