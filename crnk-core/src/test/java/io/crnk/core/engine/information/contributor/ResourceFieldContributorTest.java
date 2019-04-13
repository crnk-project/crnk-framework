package io.crnk.core.engine.information.contributor;

import com.google.common.collect.Sets;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.internal.registry.DefaultRegistryEntryBuilder;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.ProjectToTaskRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.mock.repository.ThingRepository;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.TestServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.repository.TaskToProjectRelationshipRepository;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceFieldContributorTest {

    private CoreTestContainer container;

    private ContributorRelationshipRepository contributedRepository;

    static {
        DefaultRegistryEntryBuilder.FAIL_ON_MISSING_REPOSITORY = false;
    }

    @Before
    public void setup() {
        contributedRepository = Mockito.spy(new ContributorRelationshipRepository());

        SimpleModule testModule = new SimpleModule("test");
        testModule.addRepository(new TaskRepository());
        testModule.addRepository(new ProjectRepository());
        testModule.addRepository(new ProjectToTaskRepository());
        testModule.addRepository(new ThingRepository());
        testModule.addRepository(new ScheduleRepositoryImpl());
        testModule.addRepository(contributedRepository);

        container = new CoreTestContainer();
        container.getBoot().setServiceDiscovery(new TestServiceDiscovery());
        container.addModule(testModule);
        container.boot();
    }

    @After
    public void teardown() {
        MockRepositoryUtil.clear();
    }

    @Test
    public void checkFieldAddedToResourceInformation() {
        RegistryEntry entry = container.getEntry(Task.class);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        ResourceField contributedField = resourceInformation.findFieldByName("contributedProject");
        Assert.assertNotNull(contributedField);
        Assert.assertEquals("projects", contributedField.getOppositeResourceType());
        Assert.assertEquals(SerializeType.LAZY, contributedField.getSerializeType());
    }

    @Test
    public void checkInclusionUponRequest() {
        TaskRepository repo = new TaskRepository();
        Task task = new Task();
        task.setId(1L);
        task.setName("someTask");
        repo.save(task);

        HttpRequestDispatcherImpl requestDispatcher = container.getBoot().getRequestDispatcher();

        Map<String, Set<String>> parameters = new HashMap<>();
        parameters.put("include", Sets.newHashSet("contributedProject"));
        Response response = requestDispatcher.dispatchRequest("tasks", "GET", parameters, null);

        Document document = response.getDocument();
        Resource resource = document.getCollectionData().get().get(0);
        Assert.assertEquals("1", resource.getId());
        Assert.assertEquals("tasks", resource.getType());
        Assert.assertEquals("someTask", resource.getAttributes().get("name").asText());

        Relationship relationship = resource.getRelationships().get("contributedProject");
        Assert.assertNotNull(relationship);
        ResourceIdentifier relationshipId = relationship.getSingleData().get();
        Assert.assertEquals("projects", relationshipId.getType());
        Assert.assertEquals("11", relationshipId.getId());

        List<Resource> included = document.getIncluded();
        Assert.assertEquals(1, included.size());
        Resource includedResource = included.get(0);
        Assert.assertEquals("projects", includedResource.getType());
        Assert.assertEquals("11", includedResource.getId());
        Assert.assertEquals("someProject", includedResource.getAttributes().get("name").asText());
    }


    class ContributorRelationshipRepository extends TaskToProjectRelationshipRepository implements ResourceFieldContributor {


        @Override
        @SuppressWarnings("unchecked")
        public MultivaluedMap<Long, Project> findTargets(Collection<Long> sourceIds, String fieldName, QuerySpec querySpec) {
            MultivaluedMap<Long, Project> map = new MultivaluedMap<>();

            Iterator<Long> iterator = sourceIds.iterator();
            while (iterator.hasNext()) {
                Long sourceId = iterator.next();
                if (fieldName.equals("contributedProject")) {
                    Project project = new Project();
                    project.setId(sourceId + 10);
                    project.setName("someProject");
                    map.add(sourceId, project);
                }
            }
            return map;
        }


        @Override
        public List<ResourceField> getResourceFields(ResourceFieldContributorContext context) {
            InformationBuilder.FieldInformationBuilder fieldBuilder = context.getInformationBuilder().createResourceField();
            fieldBuilder.jsonName("contributedProject");
            fieldBuilder.underlyingName("contributedProject");
            fieldBuilder.type(Project.class);
            fieldBuilder.oppositeResourceType("projects");
            fieldBuilder.lookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
            fieldBuilder.fieldType(ResourceFieldType.RELATIONSHIP);
            fieldBuilder.accessor(new ResourceFieldAccessor() {
                @Override
                public Object getValue(Object resource) {
                    return null;
                }

                @Override
                public void setValue(Object resource, Object fieldValue) {
                }

                @Override
                public Class getImplementationClass() {
                    return Project.class;
                }
            });
            return Arrays.asList(fieldBuilder.build());
        }
    }


}
