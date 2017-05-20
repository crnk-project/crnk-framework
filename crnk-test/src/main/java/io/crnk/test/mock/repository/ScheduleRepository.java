package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.test.mock.models.Schedule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

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
	@Path("repositoryActionWithResourceResult")
	Schedule repositoryActionWithResourceResult(@QueryParam(value = "msg") String msg);

	@GET
	@Path("repositoryActionWithException")
	Schedule repositoryActionWithException(@QueryParam(value = "msg") String msg);

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
