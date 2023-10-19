package io.crnk.rs.controller;

import io.crnk.rs.type.JsonApiMediaType;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Consumes(JsonApiMediaType.APPLICATION_JSON_API)
@Produces(JsonApiMediaType.APPLICATION_JSON_API)
@Path("tasks")
public class SampleControllerWithoutPrefix {

	public static final String NON_RESOURCE_RESPONSE = "NON_RESOURCE_RESPONSE";

	@GET
	@Path("sample")
	public Response getRequest() {
		return Response.ok(NON_RESOURCE_RESPONSE).build();
	}
}
