package io.crnk.core.repository;

import io.crnk.core.CoreTestModule;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultRelationBehaviorTest {

    private CrnkBoot boot;

    @Before
    public void setup() {
        SimpleModule module = new SimpleModule("test");
        module.addRepository(new InMemoryResourceRepository<>(BuilderTestTask.class));
        module.addRepository(new InMemoryResourceRepository<>(BuilderTestProject.class));
        module.addRepository(new OneRelationshipRepositoryBase() {
            @Override
            public RelationshipMatcher getMatcher() {
                RelationshipMatcher matcher = new RelationshipMatcher();
                matcher.rule().field("subTasks").add();
                return matcher;
            }

            @Override
            public Map findOneRelations(Collection sourceIds, String fieldName, QuerySpec querySpec) {
                return null;
            }
        });

        boot = new CrnkBoot();
        boot.addModule(new CoreTestModule());
        boot.addModule(module);
        boot.boot();
    }

    @Test
    public void checkMappedBy() {
        ResourceRegistry resourceRegistry = boot.getResourceRegistry();
        RegistryEntry entry = resourceRegistry.getEntry(BuilderTestProject.class);
        ResourceInformation project = entry.getResourceInformation();
        ResourceField tasks = project.findFieldByUnderlyingName("tasks");

        Assert.assertTrue(tasks.isMappedBy());
        Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OPPOSITE, tasks.getRelationshipRepositoryBehavior());
        Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, tasks.getLookupIncludeBehavior());
    }


    @Test
    public void checkRelationId() {
        ResourceRegistry resourceRegistry = boot.getResourceRegistry();
        RegistryEntry entry = resourceRegistry.getEntry(BuilderTestProject.class);
        ResourceInformation project = entry.getResourceInformation();
        ResourceField parentField = project.findFieldByUnderlyingName("parent");

        Assert.assertFalse(parentField.isMappedBy());
        Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, parentField.getRelationshipRepositoryBehavior());
        Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, parentField.getLookupIncludeBehavior());
    }

    @Test
    public void checkDefaultRelationWithoutRepository() {
        ResourceRegistry resourceRegistry = boot.getResourceRegistry();
        RegistryEntry entry = resourceRegistry.getEntry(BuilderTestTask.class);
        ResourceInformation task = entry.getResourceInformation();
        ResourceField projectField = task.findFieldByUnderlyingName("project");

        Assert.assertFalse(projectField.isMappedBy());
        Assert.assertEquals(RelationshipRepositoryBehavior.FORWARD_OWNER, projectField.getRelationshipRepositoryBehavior());
        Assert.assertEquals(LookupIncludeBehavior.NONE, projectField.getLookupIncludeBehavior());
    }

    @Test
    public void checkDefaultRelationWithRepository() {
        ResourceRegistry resourceRegistry = boot.getResourceRegistry();
        RegistryEntry entry = resourceRegistry.getEntry(BuilderTestTask.class);
        ResourceInformation task = entry.getResourceInformation();
        ResourceField subTasksField = task.findFieldByUnderlyingName("subTasks");

        Assert.assertFalse(subTasksField.isMappedBy());
        Assert.assertEquals(RelationshipRepositoryBehavior.CUSTOM, subTasksField.getRelationshipRepositoryBehavior());
        Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, subTasksField.getLookupIncludeBehavior());
    }


    @JsonApiResource(type = "task")
    public static class BuilderTestTask {
        @JsonApiId
        public String id;

        @JsonApiRelation // not backed by repository
        public Project project;

        @JsonApiRelation // backed by repository
        public List<BuilderTestTask> subTasks;

    }


    @JsonApiResource(type = "project")
    public static class BuilderTestProject {
        @JsonApiId
        public String id;

        @JsonApiRelation(mappedBy = "project")
        public Set<Task> tasks;

        @JsonApiRelationId
        public BuilderTestProject parentId;

        @JsonApiRelation
        public BuilderTestProject parent;
    }
}
