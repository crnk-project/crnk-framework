package io.crnk.rs.controller;

import io.crnk.rs.type.JsonApiMediaType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
