package io.crnk.core.engine.internal.document.mapper.lookup;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.mock.models.HierarchicalTask;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterBaseTest extends AbstractDocumentMapperTest {

    private HierarchicalTask h;

    private HierarchicalTask h0;

    private HierarchicalTask h1;

    private HierarchicalTask h11;

    private PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Before
    public void setup() {
        super.setup();

        // get repositories
        ResourceRepositoryAdapter taskRepository = container.getEntry(Task.class).getResourceRepository();
        RelationshipRepositoryAdapter relRepositoryTaskToProject =
                container.getEntry(Task.class).getRelationshipRepository("projects");
        RelationshipRepositoryAdapter relRepositoryProjectToTask =
                container.getEntry(Project.class).getRelationshipRepository("tasks");
        ResourceRepositoryAdapter projectRepository = container.getEntry(Project.class).getResourceRepository();
        ResourceRepositoryAdapter hierarchicalTaskRepository =
                container.getEntry(HierarchicalTask.class).getResourceRepository();

        // setup test data
        ResourceInformation taskInfo = container.getEntry(Task.class).getResourceInformation();
        ResourceInformation projectInfo = container.getEntry(Project.class).getResourceInformation();
        ResourceField includedTaskField = projectInfo.findRelationshipFieldByName("includedTask");
        ResourceField includedProjectField = taskInfo.findRelationshipFieldByName("includedProject");
        ResourceField includedProjectsField = taskInfo.findRelationshipFieldByName("includedProjects");
        ResourceField projectField = taskInfo.findRelationshipFieldByName("project");

        QueryAdapter projectQuey = emptyQueryAdapter(Project.class);
        QueryAdapter taskQuery = emptyQueryAdapter(Task.class);
        QueryAdapter hierarchicalTaskQuery = emptyQueryAdapter(HierarchicalTask.class);

        Project project = new Project();
        project.setId(2L);
        projectRepository.create(project, projectQuey);
        Task task = new Task();
        task.setId(1L);
        taskRepository.create(task, taskQuery);
        relRepositoryTaskToProject.setRelation(task, project.getId(), includedProjectField, projectQuey);
        relRepositoryTaskToProject.setRelation(task, project.getId(), projectField, projectQuey);
        relRepositoryTaskToProject
                .addRelations(task, Collections.singletonList(project.getId()), includedProjectsField, projectQuey);

        // setup deep nested relationship
        Task includedTask = new Task();
        includedTask.setId(3L);
        taskRepository.create(includedTask, taskQuery);
        relRepositoryProjectToTask.setRelation(project, includedTask.getId(), includedTaskField, taskQuery);
        Project deepIncludedProject = new Project();
        deepIncludedProject.setId(2L);
        projectRepository.create(project, projectQuey);
        relRepositoryTaskToProject.setRelation(includedTask, deepIncludedProject.getId(), includedProjectField, projectQuey);
        relRepositoryTaskToProject
                .addRelations(includedTask, Collections.singletonList(project.getId()), includedProjectsField, projectQuey);

        // setup hierarchy of resources
        h = new HierarchicalTask();
        h.setId(1L);
        h.setName("");

        h0 = new HierarchicalTask();
        h0.setId(2L);
        h0.setName("0");
        h0.setParent(h);

        h1 = new HierarchicalTask();
        h1.setId(3L);
        h1.setName("1");
        h1.setParent(h);

        h11 = new HierarchicalTask();
        h11.setId(4L);
        h11.setName("11");
        h11.setParent(h1);

        h.setChildren(Arrays.asList(h0, h1));
        h0.setChildren(new ArrayList<HierarchicalTask>());
        h1.setChildren(Arrays.asList(h11));
        h11.setChildren(new ArrayList<HierarchicalTask>());

        hierarchicalTaskRepository.create(h, hierarchicalTaskQuery);
        hierarchicalTaskRepository.create(h0, hierarchicalTaskQuery);
        hierarchicalTaskRepository.create(h1, hierarchicalTaskQuery);
        hierarchicalTaskRepository.create(h11, hierarchicalTaskQuery);

    }

    private QueryAdapter emptyQueryAdapter(Class<?> resourceClass) {
        return container.toQueryAdapter(new QuerySpec(resourceClass));
    }

    @Test
    public void paginationMustOnlyHappenRootButNotInclusions() {
        HierarchicalTask hDetached = new HierarchicalTask();
        hDetached.setId(1L);
        hDetached.setName("");

        QuerySpec querySpec = new QuerySpec(HierarchicalTask.class);
        querySpec.setOffset(0L);
        querySpec.setLimit(1L);
        querySpec.includeRelation(Arrays.asList("children"));

        Document document = mapper.toDocument(toResponse(hDetached), toAdapter(querySpec), mappingConfig).get();

        Relationship childrenRelationship = document.getSingleData().get().getRelationships().get("children");
        List<ResourceIdentifier> childIds = childrenRelationship.getCollectionData().get();
        Assert.assertEquals(2, childIds.size());
    }

    @Test
    public void paginationMustHappenRootAndInclusions() {
        Mockito.when(propertiesProvider.getProperty(Mockito.eq(CrnkProperties.INCLUDE_PAGING_ENABLED))).thenReturn("true");
        setup();

        HierarchicalTask hDetached = new HierarchicalTask();
        hDetached.setId(1L);
        hDetached.setName("");

        QuerySpec querySpec = new QuerySpec(HierarchicalTask.class);
        querySpec.setOffset(0L);
        querySpec.setLimit(1L);
        querySpec.includeRelation(Arrays.asList("children"));

        Document document = mapper.toDocument(toResponse(hDetached), toAdapter(querySpec), mappingConfig).get();

        Relationship childrenRelationship = document.getSingleData().get().getRelationships().get("children");
        List<ResourceIdentifier> childIds = childrenRelationship.getCollectionData().get();
        Assert.assertEquals(1, childIds.size());
    }

    protected final PropertiesProvider getPropertiesProvider() {
        return propertiesProvider;
    }


    @Test
    public void includeOneRelationLookup() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeRelation(Arrays.asList("includedProject"));

        Task task = new Task();
        task.setId(1L);

        Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
        Resource taskResource = document.getSingleData().get();

        Relationship relationship = taskResource.getRelationships().get("includedProject");
        assertNotNull(relationship);
        assertNotNull(relationship.getSingleData());

        List<Resource> included = document.getIncluded();
        assertEquals(1, included.size());
    }

    @Test
    public void includeManyRelationLookup() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeRelation(Arrays.asList("includedProjects"));

        Project project = new Project();
        project.setId(2L);

        Task task = new Task();
        task.setId(1L);
        task.setIncludedProjects(Arrays.asList(project));

        Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
        Resource taskResource = document.getSingleData().get();

        Relationship relationship = taskResource.getRelationships().get("includedProjects");
        assertNotNull(relationship);
        assertEquals(1, relationship.getCollectionData().get().size());
    }

    @Test
    public void includeOneDeepNestedRelationLookup() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeRelation(Arrays.asList("includedProject", "includedTask", "includedProject"));

        Project project = new Project();
        project.setId(2L);

        Task task = new Task();
        task.setId(1L);
        task.setIncludedProject(project);
        project.setIncludedTask(task);

        Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
        Resource taskResource = document.getSingleData().get();

        Relationship oneRelationship = taskResource.getRelationships().get("includedProject");
        assertNotNull(oneRelationship);
        assertNotNull(oneRelationship.getSingleData());

        List<Resource> includes = document.getIncluded();
        assertEquals(1, includes.size());
        Resource includedResource = includes.get(0);
        assertEquals("projects", includedResource.getType());
        assertNotNull(includedResource.getRelationships().get("includedTask"));
        assertNotNull(includedResource.getRelationships().get("includedTask").getData());
    }

    @Test
    public void includeManyDeepNestedRelationLookup() {
        Project project = new Project();
        project.setId(2L);

        Task task = new Task();
        task.setId(1L);
        task.setIncludedProjects(Arrays.asList(project));
        project.setIncludedTask(task);


        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeRelation(Arrays.asList("includedProjects", "includedTask", "includedProject"));

        Document document = mapper.toDocument(toResponse(task), toAdapter(querySpec), mappingConfig).get();
        Resource taskResource = document.getSingleData().get();

        Relationship manyRelationship = taskResource.getRelationships().get("includedProjects");
        assertNotNull(manyRelationship);
        List<ResourceIdentifier> relationshipData = manyRelationship.getCollectionData().get();
        assertNotNull(relationshipData.get(0).getId());

        List<Resource> includes = document.getIncluded();
        assertEquals(1, includes.size());
        Resource includedResource = includes.get(0);
        assertEquals("projects", includedResource.getType());
        assertNotNull(includedResource.getRelationships().get("includedTask"));
        assertNotNull(includedResource.getRelationships().get("includedTask").getData());
    }

    @Test
    public void includeByDefaultSerializeNLevels() {
        Project project = new Project();
        project.setId(1L);

        Task task = new Task().setId(2L);
        project.setTask(task);

        Project projectDefault = new Project().setId(3L);
        task.setProject(projectDefault);

        mapper = new DocumentMapper(container.getResourceRegistry(), objectMapper, new NullPropertiesProvider(), resourceFilterDirectory,
                new ImmediateResultFactory(), null, container.getModuleRegistry().getUrlBuilder());

        QuerySpec querySpec = new QuerySpec(Project.class);
        querySpec.includeRelation(Collections.singletonList("task"));

        Document document = mapper.toDocument(toResponse(project), toAdapter(querySpec), mappingConfig).get();
        Resource projectResource = document.getSingleData().get();

        Relationship relationship = projectResource.getRelationships().get("task");
        assertNotNull(relationship);
        assertEquals("2", relationship.getSingleData().get().getId());

        assertNotNull(document.getIncluded());
        assertEquals(2, document.getIncluded().size());
        List<Resource> resources = document.getIncluded();
        assertEquals("projects", resources.get(0).getType());
        assertEquals("3", resources.get(0).getId());
        assertEquals("tasks", resources.get(1).getType());
        assertEquals("2", resources.get(1).getId());

    }
}
