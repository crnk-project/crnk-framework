package io.crnk.core.engine.internal.document.mapper.lookup;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.mock.models.FancyProject;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterInheritanceTest extends AbstractDocumentMapperTest {


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Before
    public void setup() {
        super.setup();

        // get repositories
        ResourceRepositoryAdapter taskRepository = container.getEntry(Task.class).getResourceRepository();
        RelationshipRepositoryAdapter relRepositoryTaskToProject =
                container.getEntry(Task.class).getRelationshipRepository("includedProjects");
        ResourceRepositoryAdapter projectRepository = container.getEntry(Project.class).getResourceRepository();

        // setup test data
        ResourceInformation taskInfo = container.getEntry(Task.class).getResourceInformation();
        ResourceField includedProjectsField = taskInfo.findRelationshipFieldByName("includedProjects");

        QueryAdapter emptyProjectQuery = container.toQueryAdapter(new QuerySpec(Project.class));
        QueryAdapter emptyTaskQuery = container.toQueryAdapter(new QuerySpec(Task.class));

        Project project1 = new Project();
        project1.setId(3L);
        projectRepository.create(project1, emptyProjectQuery);

        FancyProject project2 = new FancyProject();
        project2.setId(ProjectRepository.FANCY_PROJECT_ID);
        projectRepository.create(project2, emptyProjectQuery);


        Task task = new Task();
        task.setId(1L);
        taskRepository.create(task, emptyTaskQuery);
        relRepositoryTaskToProject.addRelations(task, Collections.singletonList(project1.getId()), includedProjectsField, emptyProjectQuery);
        relRepositoryTaskToProject.addRelations(task, Collections.singletonList(project2.getId()), includedProjectsField, emptyProjectQuery);
    }

    @Test
    public void testPolymorhRelationship() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeRelation(Arrays.asList("includedProjects"));

        Task task = new Task();
        task.setId(1L);

        Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
        Resource taskResource = document.getSingleData().get();

        Relationship relationship = taskResource.getRelationships().get("includedProjects");
        assertNotNull(relationship);

        List<ResourceIdentifier> projects = relationship.getCollectionData().get();
        assertEquals(2, projects.size());

        List<Resource> included = document.getIncluded();
        assertEquals(2, included.size());
        Assert.assertEquals("projects", included.get(1).getType());
        Assert.assertEquals("fancy-projects", included.get(0).getType());
    }
}
