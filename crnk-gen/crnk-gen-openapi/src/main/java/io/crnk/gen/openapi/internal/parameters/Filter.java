package io.crnk.gen.openapi.internal.parameters;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

public class Filter {
	public static Parameter parameter() {
    return new Parameter().name("filter")
  				.description("Customizable query (experimental)")
  				.in("query")
  				.schema(
  						new ObjectSchema()
  								.addProperties("AND", new ObjectSchema())
  								.addProperties("OR", new ObjectSchema())
  								.addProperties("NOT", new ObjectSchema())
  								.additionalProperties(true));
  }
}
