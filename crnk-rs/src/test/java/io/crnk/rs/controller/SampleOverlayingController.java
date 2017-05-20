package io.crnk.rs.controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Consumes(MediaType.WILDCARD)
@Produces(MediaType.TEXT_PLAIN)
@Path("tasks")
public class SampleOverlayingController {

	public static final String NON_RESOURCE_OVERLAY_RESPONSE = "NON_RESOURCE_OVERLAY_RESPONSE";

	@GET
	@Path("{id}")
	public Response getRequest(@PathParam("id") final int taskId) {
		return Response.ok(NON_RESOURCE_OVERLAY_RESPONSE).build();
	}

	@POST
	@Path("{id}")
	public Response postRequest(@PathParam("id") final int taskId) {
		return Response.ok(NON_RESOURCE_OVERLAY_RESPONSE).build();
	}

}
