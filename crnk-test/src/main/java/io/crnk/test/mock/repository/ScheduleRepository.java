package io.crnk.test.mock.repository;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.Link;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;
import io.crnk.test.mock.models.Schedule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

// tag::annotation[]
@Path("schedules")
@Produces(HttpHeaders.JSONAPI_CONTENT_TYPE)
// end::annotation[]
// tag::doc[]
public interface ScheduleRepository extends ResourceRepository<Schedule, Long> {
	// end::doc[]

	// tag::services[]
	@GET
	@Path("repositoryAction")
	@Produces(MediaType.TEXT_HTML)
	String repositoryAction(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionJsonApi")
	String repositoryActionJsonApi(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithJsonApiResponse")
	String repositoryActionWithJsonApiResponse(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithResourceResult")
	Schedule repositoryActionWithResourceResult(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithException")
	Schedule repositoryActionWithException(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithNullResponse")
	@Produces(MediaType.TEXT_HTML)
	String repositoryActionWithNullResponse();

	@GET
	@Path("repositoryActionWithNullResponseJsonApi")
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

		public Link objLink = new DefaultLink("value");

		public String stringLink = "value";
	}

	class ScheduleListMeta extends DefaultPagedMetaInformation {

		public String name = "value";

	}
}
// end::doc[]
