package io.crnk.test.mock.repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.rs.type.JsonApiMediaType;
import io.crnk.test.mock.models.Schedule;

// tag::annotation[]
@Path("schedules")
// end::annotation[]
// tag::doc[]
public interface ScheduleRepository extends ResourceRepositoryV2<Schedule, Long> {
// end::doc[]

	// tag::services[]
	@GET
	@Path("repositoryAction")
	String repositoryAction(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithJsonApiResponse")
	@Produces(JsonApiMediaType.APPLICATION_JSON_API)
	String repositoryActionWithJsonApiResponse(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithResourceResult")
	Schedule repositoryActionWithResourceResult(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithException")
	Schedule repositoryActionWithException(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithNullResponse")
	String repositoryActionWithNullResponse();

	@GET
	@Path("repositoryActionWithNullResponseJsonApi")
	@Produces(JsonApiMediaType.APPLICATION_JSON_API)
	String repositoryActionWithNullResponseJsonApi();

	@GET
	@Path("{id}/resourceAction")
	String resourceAction(@PathParam("id") long id, @QueryParam(value = "msg") String msg);
	// end::services[]

	// tag::doc[]
	@Override
	ScheduleList findAll(QuerySpec querySpec);

	class ScheduleList extends ResourceListBase<Schedule, ScheduleListMeta, ScheduleListLinks> {

	}

	class ScheduleListLinks extends DefaultPagedLinksInformation implements LinksInformation {

		public String name = "value";
	}

	class ScheduleListMeta implements MetaInformation {

		public String name = "value";

	}
}
// end::doc[]
