package io.crnk.client;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Test;

import java.util.Arrays;

public class ResourceRepositoryStubTest extends AbstractClientTest {


    @Test(expected = BadRequestException.class)
    public void rejectInvalidQuerySpecForFindAll() {
        ScheduleRepository repo = client.getRepositoryForInterface(ScheduleRepository.class);
        QuerySpec querySpec = new QuerySpec(Project.class); // not for Scheduler!
        repo.findAll(querySpec);
    }

    @Test(expected = BadRequestException.class)
    public void rejectInvalidQuerySpecForFindByIds() {
        ScheduleRepository repo = client.getRepositoryForInterface(ScheduleRepository.class);
        QuerySpec querySpec = new QuerySpec(Project.class); // not for Scheduler!
        repo.findAll(Arrays.asList(1L), querySpec);
    }

    @Test(expected = BadRequestException.class)
    public void rejectInvalidQuerySpecOneRelationship() {
        OneRelationshipRepository<Schedule, Object, Project, Object> repo = client.getOneRepositoryForType(Schedule.class, Project.class);
        QuerySpec querySpec = new QuerySpec(Schedule.class); // not for Project!
        repo.findOneRelations(Arrays.asList(1L), "project", querySpec);
    }

    @Test(expected = BadRequestException.class)
    public void rejectInvalidQuerySpecManyRelationship() {
        ManyRelationshipRepository<Schedule, Object, Project, Object> repo = client.getManyRepositoryForType(Schedule.class, Project.class);
        QuerySpec querySpec = new QuerySpec(Schedule.class); // not for Project!
        repo.findManyRelations(Arrays.asList(1L), "projects", querySpec);
    }
}
