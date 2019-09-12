package io.crnk.gen.openapi.internal;

import io.crnk.core.engine.http.HttpStatus;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

public class OASGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(OASGenerator.class);
  private OpenAPI openApi;

  public OASGenerator(OpenAPI baseOpenAPI) {
    openApi = baseOpenAPI;
    openApi.getComponents().schemas(generateStandardSchemas());
    openApi.getComponents().parameters(getStandardPagingParameters());
    openApi.getComponents().addParameters("contentType", generateStandardContentTypeParameter());
    openApi.getComponents().addParameters("filter", generateStandardFilterParameter());
		openApi.getComponents().responses(generateStandardApiResponses());
  }

  public OpenAPI getOpenApi() {
    return openApi;
  }

  public OpenAPI register(OASResource oasResource) {
		oasResource.getComponentParameters().forEach(openApi.getComponents()::addParameters);
		oasResource.getComponentSchemas().forEach(openApi.getComponents()::addSchemas);
		oasResource.getComponentResponses().forEach(openApi.getComponents()::addResponses);
  	return openApi;
	}

  // SCHEMAS

	/*
		Generate default schemas that are common across the api.
		For example, in JSON:API, the error response is common across all APIs
	 */
	private Map<String, Schema> generateStandardSchemas() {
		Map<String, Schema> schemas = new LinkedHashMap<>();

		// Standard Error Schema
		schemas.put("ApiError", jsonApiError());

		// Standard wrapper responses for single & multiple records
		schemas.put("ResponseMixin", responseMixin());
		schemas.put("ListResponseMixin", listResponseMixin());

		return schemas;
	}

private static Schema jsonApiError() {
	return new Schema()
			.type("object")
			.addProperties(
					"id",
					new Schema()
							.type("string")
							.description("a unique identifier for this particular occurrence of the problem"))
			.addProperties("links",
					new Schema()
							.type("object")
							.addProperties(
									"about",
									new Schema()
											.type("string")
											.description("a link that leads to further details about this particular occurrence of the problem")))
			.addProperties(
					"status",
					new Schema()
							.type("string")
							.description("the HTTP status code applicable to this problem, expressed as a string value"))
			.addProperties(
					"code",
					new Schema()
							.type("string")
							.description("an application-specific error code, expressed as a string value"))
			.addProperties(
					"title",
					new Schema()
							.type("string")
							.description("a short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization"))
			.addProperties(
					"detail",
					new Schema()
							.type("string")
							.description("a human-readable explanation specific to this occurrence of the problem. Like 'title', this field’s value can be localized."))
			.addProperties(
					"source",
					new Schema()
							.type("object")
							.addProperties(
									"pointer",
									new Schema()
											.type("string")
											.description("a JSON Pointer [RFC6901] to the associated entity in the request document"))
							.addProperties(
									"parameter",
									new Schema()
											.type("string")
											.description("a string indicating which URI query parameter caused the error")))
			.addProperties(
					"meta",
					new Schema()
							.additionalProperties(true)
							.description("a meta object containing non-standard meta-information about the error"));
}

 private static Schema responseMixin() {
		return new Schema()
				.type("object")
				.description("A JSON-API document with a single resource")
				.addProperties(
						"errors",
						new ArraySchema().items(new Schema().$ref("ApiError")))
				.addProperties(
						"jsonapi",
						new Schema()
								.type("object")
								.addProperties(
										"version",
										new Schema().type("string")))
				.addProperties(
						"links",
						new Schema().addProperties(
								"self",
								new Schema()
										.type("string")
										.description("the link that generated the current response document")))
				.addProperties(
						"included",
						new ArraySchema()
								.items(
										new Schema()
												.type("object")
												.addProperties(
														"type",
														new Schema()
																.type("string")
																.description("The JSON:API resource type"))
												.addProperties(
														"id",
														new Schema()
																.type("string")
																.description("The JSON:API resource ID")))
								.description("Included resources"));
	}

 private static Schema listResponseMixin() {
		return new Schema()
				.type("object")
				.description("A page of results")
				.addProperties(
						"jsonapi",
						new Schema()
								.type("object")
								.addProperties(
										"version",
										new Schema().type("string")))
				.addProperties(
						"errors",
						new ArraySchema().items(new Schema().$ref("ApiError")))
				.addProperties(
						"meta",
						new Schema()
								.type("object")
// TODO: Determine if this is supported
//								.addProperties(
//										"total-pages",
//										new Schema()
//												.type("integer")
//												.description("The total number of pages available"))
								.addProperties(
										"totalResourceCount",
										new Schema()
												.type("integer")
												.description("The total number of items available"))
								.additionalProperties(true))
				.addProperties(
						"links",
						new Schema()
								.type("object")
								.addProperties(
										"self",
										new Schema()
												.type("string")
												.description("Link to this page of results"))
								.addProperties(
										"prev",
										new Schema()
												.type("string")
												.description("Link to the previous page of results"))
								.addProperties(
										"next",
										new Schema()
												.type("string")
												.description("Link to the next page of results"))
								.addProperties(
										"last",
										new Schema()
												.type("string")
												.description("Link to the last page of results"))
								.addProperties(
										"first",
										new Schema()
												.type("string")
												.description("Link to the first page of results")));
	}

  // PARAMETERS

  private Parameter generateStandardFilterParameter() {
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

  private Parameter generateStandardContentTypeParameter() {
 		return new Parameter()
         .name("Content-Type")
         .in("header")
         .schema(
             new StringSchema()
                 ._default("application/vnd.api+json")
                 ._enum(Arrays.asList("application/vnd.api+json", "application/json")))
         .required(true);
 	}


  private Map<String, Parameter> getStandardPagingParameters() {
 		boolean NumberSizePagingBehavior = false;
 		Map<String, Parameter> parameters = new LinkedHashMap<>();
 		parameters.put(
 				"pageLimit",
 				new Parameter().name("page[limit]")
 						.description("Max number of items")
 						.in("query")
 						.schema(
 								new IntegerSchema()
 										.format("int64")
 										._default(100)  // TODO: resolve from application.properties.crnk.default-page-limit=20
 										.maximum(BigDecimal.valueOf(1000))));  // TODO: resolve from application.properties.crnk.max-page-limit=1000
 		parameters.put(
 				"pageOffset",
 				new Parameter().name("page[offset]")
 						.description("Page offset")
 						.in("query")
 						.schema(
 								new IntegerSchema()
 										.format("int64")
 										._default(0)));

 		if (NumberSizePagingBehavior) {  // TODO: Figure out how to determine this
 			parameters.put(
 					"pageNumber",
 					new Parameter().name("page[number]")
 							.description("Page number")
 							.in("query")
 							.schema(
 									new IntegerSchema()
 											.format("int64")
 											._default(1)));

 			parameters.put(
 					"pageSize",
 					new Parameter().name("page[size]")
 							.description("Page size")
 							.in("query")
 							.schema(
 									new IntegerSchema()
 											.format("int64")
 											._default(0)));  // TODO: resolve from application.properties.crnk.default-page-limit=20
 		}

 		return parameters;
 	}

 	// RESPONSES

 	private Map<String, ApiResponse> generateStandardApiResponses() {
 		return OASUtils.mergeApiResponses(generateStandardApiSuccessResponses(), generateStandardApiErrorResponses());
 	}

	private Map<String, ApiResponse> generateStandardApiSuccessResponses() {
		Map<String, ApiResponse> responses = new LinkedHashMap<>();
		responses.put(
				"202",
				new ApiResponse()
								.description("Accepted")
								.content(new Content()
										.addMediaType("application/vnd.api+json",
												new MediaType()
														.schema(
																new ObjectSchema()
																		.addProperties(
																				"id",
																				new StringSchema()
																						.description("a unique identifier for this pending action"))))));
		responses.put(
				"204",
				new ApiResponse()
						.description("No Content"));

		return responses;
	}

  private Map<String, ApiResponse> generateStandardApiErrorResponses() {
 		Map<String, ApiResponse> responses = new LinkedHashMap<>();

 		List<Integer> responseCodes = getStandardHttpStatusCodes();
 		for (Integer responseCode : responseCodes) {
 			if (responseCode >= 400 && responseCode <= 599) {
 				ApiResponse apiResponse = new ApiResponse();
 				apiResponse.description(HttpStatus.toMessage(responseCode));
 				apiResponse.content(new Content()
 						.addMediaType("application/json",
 								new MediaType().schema(new Schema().$ref("ApiError")))
 				);
 				responses.put(responseCode.toString(), apiResponse);
 			}
 		}

 		return responses;
 	}

  /*
 		Crnk maintains a list of HTTP status codes in io.crnk.core.engine.http.HttpStatus
 		as static fields. Iterate through and collect them into a list for use elsewhere.
 	 */
 	private List<Integer> getStandardHttpStatusCodes() {
 		List<Integer> responseCodes = new ArrayList<>();

 		Field[] fields = HttpStatus.class.getDeclaredFields();
 		for (Field f : fields) {
 			if (Modifier.isStatic(f.getModifiers())) {
 				try {
 					responseCodes.add(f.getInt(null));
 				} catch (IllegalAccessException ignore) {
 				}
 			}
 		}
 		return responseCodes;
 	}
}
