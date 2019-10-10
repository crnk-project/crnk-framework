package io.crnk.meta;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.meta.model.MetaArrayType;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.MetaListType;
import io.crnk.meta.model.MetaMapType;
import io.crnk.meta.model.MetaPrimitiveType;
import io.crnk.meta.model.MetaSetType;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceAction;
import io.crnk.meta.model.resource.MetaResourceAction.MetaRepositoryActionType;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.meta.model.resource.MetaResourceRepository;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskStatus;
import io.crnk.test.mock.models.TaskSubType;
import io.crnk.test.mock.models.types.ProjectData;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceMetaProviderTest extends AbstractMetaTest {

    private MetaLookupImpl lookup;

    private ResourceMetaProvider resourceProvider;

    @Before
    public void setup() {
        super.setup();

        resourceProvider = new ResourceMetaProvider();

        lookup = new MetaLookupImpl();
        lookup.setModuleContext(container.getModuleRegistry().getContext());
        lookup.addProvider(resourceProvider);
        lookup.initialize();
    }

    @Test
    public void testReadWriteMethods() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);

        MetaAttribute idAttr = meta.getAttribute("id");
        Assert.assertEquals("getId", idAttr.getReadMethod().getName());
        Assert.assertEquals("setId", idAttr.getWriteMethod().getName());

        Schedule schedule = new Schedule();
        schedule.setTasks(new HashSet<>());
        schedule.setId(13L);
        Assert.assertEquals(13L, idAttr.getValue(schedule));

        idAttr.setValue(schedule, 14L);
        Assert.assertEquals(14L, schedule.getId().longValue());

        // TODO meta ttribute & renaming
		/*
		MetaAttribute listAttr = meta.getAttribute("taskSet");
		Task task = new Task();
		task.setId(12L);
		listAttr.addValue(schedule, task);
		Assert.assertEquals(1, schedule.getTasks().size());
		listAttr.removeValue(schedule, task);
		Assert.assertEquals(0, schedule.getTasks().size());
		 */
    }

    @Test
    public void getVersionAttribute() {
        // TODO setup versioning concept in json api layer
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        Assert.assertNull(meta.getVersionAttribute());
    }


    @Test
    public void checkUseOfJsonNaming() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        // this is the json name, not java (desc)!
        MetaAttribute attr = meta.getAttribute("description");
        Assert.assertNotNull(attr);
    }

    @Test
    public void resolvePath() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);

        MetaAttributePath metaAttributes = meta.resolvePath(Arrays.asList("project", "name"));
        Assert.assertEquals(2, metaAttributes.length());
        Assert.assertEquals("project", metaAttributes.getElement(0).getName());
        Assert.assertEquals("name", metaAttributes.getElement(1).getName());
    }

    @Test
    public void resolvePathWithSubType() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        MetaAttributePath metaAttributes = meta.resolvePath(Arrays.asList("subTypeValue"), true);
        Assert.assertEquals(1, metaAttributes.length());
        Assert.assertEquals("subTypeValue", metaAttributes.getElement(0).getName());
    }


    @Test
    public void getSubTypes() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        List<MetaDataObject> subTypes = meta.getSubTypes(true, false);
        Assert.assertEquals(1, subTypes.size());
        MetaDataObject subType = subTypes.iterator().next();
        Assert.assertEquals(TaskSubType.class, subType.getImplementationClass());
    }


    @Test
    public void getSubTypesOrSelf() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        List<MetaDataObject> subTypes = meta.getSubTypes(true, true);
        Assert.assertEquals(2, subTypes.size());
        Assert.assertTrue(subTypes.contains(meta));
    }

    @Test
    public void testGetAnnotation() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        MetaAttribute idAttr = meta.getAttribute("id");
        Assert.assertNotNull(idAttr.getAnnotation(JsonApiId.class));
    }

    @Test
    public void testGetAnnotations() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        MetaAttribute idAttr = meta.getAttribute("id");
        Assert.assertEquals(1, idAttr.getAnnotations().size());
    }

    @Test
    public void testPreserveAttributeOrder() {
        ResourceInformation resourceInformation = container.getEntry(Schedule.class).getResourceInformation();
        List<ResourceField> fields = resourceInformation.getFields();
        Assert.assertEquals("id", fields.get(0).getUnderlyingName());
        Assert.assertEquals("name", fields.get(1).getUnderlyingName());
        Assert.assertEquals("desc", fields.get(2).getUnderlyingName());
        Assert.assertEquals("tasks", fields.get(3).getUnderlyingName());
        Assert.assertEquals("project", fields.get(4).getUnderlyingName());
        Assert.assertEquals("projects", fields.get(5).getUnderlyingName());
        Assert.assertEquals("status", fields.get(6).getUnderlyingName());
        Assert.assertEquals("delayed", fields.get(7).getUnderlyingName());
        Assert.assertEquals("customData", fields.get(8).getUnderlyingName());

        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        List<? extends MetaAttribute> attributes = meta.getAttributes();
        Assert.assertEquals("id", attributes.get(0).getName());
        Assert.assertEquals("name", attributes.get(1).getName());
        Assert.assertEquals("description", attributes.get(2).getName());
        Assert.assertEquals("taskSet", attributes.get(3).getName());
        Assert.assertEquals("project", attributes.get(4).getName());
        Assert.assertEquals("projects", attributes.get(5).getName());
        Assert.assertEquals("status", attributes.get(6).getName());
        Assert.assertEquals("delayed", attributes.get(7).getName());
        Assert.assertEquals("customData", attributes.get(8).getName());
    }

    @Test
    public void testUnderlyingNameForNestedObject() {
        MetaDataObject meta = resourceProvider.getMeta(ProjectData.class);
        MetaAttribute attribute = meta.getAttribute("due");
        Assert.assertEquals("due", attribute.getName());
        Assert.assertEquals("dueDate", attribute.getUnderlyingName());
    }

    @Test
    public void testUnderlyingNameForResource() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        MetaAttribute attribute = meta.getAttribute("taskSet");
        Assert.assertEquals("taskSet", attribute.getName());
        Assert.assertEquals("tasks", attribute.getUnderlyingName());
    }

    @Test
    public void testPrimaryKeyNotNullable() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        MetaKey primaryKey = meta.getPrimaryKey();
        MetaAttribute idField = primaryKey.getElements().get(0);
        Assert.assertFalse(idField.isNullable());

        Assert.assertNotNull(idField.getAnnotation(JsonApiId.class));
        Assert.assertEquals(1, idField.getAnnotations().size());
    }

    @Test
    public void testRegularFieldNullable() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        MetaAttribute taskField = meta.getAttribute("project");
        Assert.assertTrue(taskField.isNullable());
    }

    @Test
    public void testPrimitiveFieldNotNullable() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        MetaAttribute taskField = meta.getAttribute("delayed");
        Assert.assertFalse(taskField.isNullable());
    }

    @Test
    public void testInheritanceOnResource() {
        MetaResource meta = resourceProvider.getMeta(TaskSubType.class);

        Assert.assertNotNull(meta.getAttribute("subTypeValue"));
        Assert.assertNotNull(meta.getAttribute("name"));
        Assert.assertNotNull(meta.getAttribute("id"));

        MetaDataObject superType = meta.getSuperType();
        Assert.assertEquals(MetaResource.class, superType.getClass());
        Assert.assertFalse(superType.hasAttribute("subTypeValue"));
        Assert.assertNotNull(superType.getAttribute("name"));
        Assert.assertNotNull(superType.getAttribute("id"));

        MetaKey primaryKey = meta.getPrimaryKey();
        Assert.assertNotNull("id", primaryKey.getName());
        Assert.assertEquals(1, primaryKey.getElements().size());
        Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
        Assert.assertSame(primaryKey.getElements().get(0), meta.getAttribute("id"));
        Assert.assertTrue(meta.getPrimaryKey().isUnique());

        Assert.assertEquals(primaryKey.getElements().get(0), primaryKey.getUniqueElement());

        Assert.assertEquals("12", primaryKey.toKeyString(12));
    }

    @Test
    public void testResourceProperties() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);

        Assert.assertEquals("schedule", meta.getResourceType());
        Assert.assertEquals("Schedule", meta.getName());
        Assert.assertEquals("resources.schedule", meta.getId());

        Assert.assertEquals(Schedule.class, meta.getImplementationClass());
        Assert.assertEquals(Schedule.class, meta.getImplementationType());
        Assert.assertNull(meta.getParent());
        Assert.assertTrue(meta.getSubTypes().isEmpty());
    }

    @Test
    public void testRenamedOppositeRelationship() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);
        MetaAttribute taskSetAttr = meta.getAttribute("taskSet");
        Assert.assertEquals("taskSet", taskSetAttr.getName());
        MetaAttribute oppositeAttribute = taskSetAttr.getOppositeAttribute();
        Assert.assertNotNull(oppositeAttribute);
        Assert.assertEquals("schedule", oppositeAttribute.getName());
        Assert.assertSame(taskSetAttr, oppositeAttribute.getOppositeAttribute());
    }

    @Test
    public void testLinksAttribute() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        MetaResourceField attr = (MetaResourceField) meta.getAttribute("linksInformation");
        Assert.assertEquals("linksInformation", attr.getName());
        Assert.assertEquals("resources.tasks.linksInformation", attr.getId());
        Assert.assertFalse(attr.isLazy());
        Assert.assertFalse(attr.isMeta());
        Assert.assertTrue(attr.isLinks());
        Assert.assertNull(attr.getOppositeAttribute());
        Assert.assertEquals(Task.TaskLinks.class, attr.getType().getImplementationClass());
    }

    @Test
    public void testMetaAttribute() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        MetaResourceField attr = (MetaResourceField) meta.getAttribute("metaInformation");
        Assert.assertEquals("metaInformation", attr.getName());
        Assert.assertEquals("resources.tasks.metaInformation", attr.getId());
        Assert.assertFalse(attr.isLazy());
        Assert.assertTrue(attr.isMeta());
        Assert.assertFalse(attr.isLinks());
        Assert.assertNull(attr.getOppositeAttribute());
        Assert.assertEquals(Task.TaskMeta.class, attr.getType().getImplementationClass());
    }

    @Test
    public void testNestedObject() {
        MetaResource meta = resourceProvider.getMeta(Project.class);
        MetaAttribute data = meta.getAttribute("data");
        MetaType type = data.getType();
        Assert.assertEquals("resources.types.projectdata", type.getId());
    }


    @Test
    public void testNestedAttributeRenaming() {
        MetaResource meta = resourceProvider.getMeta(Project.class);
        MetaAttribute data = meta.getAttribute("data");
        MetaJsonObject type = (MetaJsonObject) data.getType();
        Assert.assertEquals("resources.types.projectdata", type.getId());

        Assert.assertFalse(type.hasAttribute("dueDate"));
        Assert.assertNotNull(type.getAttribute("due"));

    }


    @Test
    public void testSingleValuedAttribute() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        MetaResourceField attr = (MetaResourceField) meta.getAttribute("name");
        Assert.assertEquals("name", attr.getName());
        Assert.assertFalse(attr.isLazy());
        Assert.assertFalse(attr.isMeta());
        Assert.assertFalse(attr.isLinks());
        Assert.assertFalse(attr.isAssociation());

        Assert.assertTrue(attr.isSortable());
        Assert.assertTrue(attr.isFilterable());
        Assert.assertTrue(attr.isInsertable());
        Assert.assertTrue(attr.isUpdatable());
    }

    @Test
    public void testArrayAttribute() {
        MetaResource meta = resourceProvider.getMeta(Project.class);

        MetaResourceField dataField = (MetaResourceField) meta.getAttribute("data");
        Assert.assertEquals("data", dataField.getName());

        MetaJsonObject projectData = (MetaJsonObject) dataField.getType();

        MetaAttribute keywordField = projectData.getAttribute("keywords");

        Assert.assertFalse(keywordField.isLazy());
        Assert.assertFalse(keywordField.isAssociation());
        Assert.assertTrue(keywordField.isInsertable());
        Assert.assertTrue(keywordField.isUpdatable());

        Assert.assertEquals(MetaArrayType.class, keywordField.getType().getClass());
        Assert.assertTrue(keywordField.getType().getElementType() instanceof MetaPrimitiveType);

        Assert.assertEquals("string$array", keywordField.getType().getName());
        Assert.assertEquals("base.string$array", keywordField.getType().getId());

        // FIXME support crnk annotations
        // Assert.assertTrue(dataField.isSortable());
        // Assert.assertTrue(dataField.isFilterable());
    }

    @Test
    public void testMapAttribute() {
        MetaResource meta = resourceProvider.getMeta(Project.class);

        MetaResourceField dataField = (MetaResourceField) meta.getAttribute("data");
        Assert.assertEquals("data", dataField.getName());

        MetaJsonObject projectData = (MetaJsonObject) dataField.getType();

        MetaAttribute keywordField = projectData.getAttribute("customData");

        Assert.assertFalse(keywordField.isLazy());
        Assert.assertFalse(keywordField.isAssociation());
        Assert.assertTrue(keywordField.isInsertable());
        Assert.assertTrue(keywordField.isUpdatable());

        Assert.assertTrue(keywordField.getType() instanceof MetaMapType);
        MetaMapType mapType = (MetaMapType) keywordField.getType();
        Assert.assertTrue(keywordField.getType().getElementType() instanceof MetaPrimitiveType);
        Assert.assertTrue(mapType.getKeyType() instanceof MetaPrimitiveType);

        Assert.assertNotNull(mapType.asMap());
        Assert.assertTrue(mapType.isMap());
        Assert.assertFalse(mapType.isCollection());
    }


    @Test
    public void testEnum() {
        MetaResource meta = resourceProvider.getMeta(Task.class);
        MetaResourceField statusField = (MetaResourceField) meta.getAttribute("status");
        Assert.assertEquals(TaskStatus.class, statusField.getType().getImplementationClass());

        MetaEnumType statusType = (MetaEnumType) statusField.getType();
        Assert.assertEquals(3, statusType.getChildren().size());
        Assert.assertEquals("OPEN", statusType.getChildren().get(0).getName());
        Assert.assertEquals("INPROGRESS", statusType.getChildren().get(1).getName());
        Assert.assertEquals("CLOSED", statusType.getChildren().get(2).getName());
    }

    @Test
    public void testSingleValuedRelation() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        MetaResourceField attr = (MetaResourceField) meta.getAttribute("schedule");
        Assert.assertEquals("schedule", attr.getName());
        Assert.assertEquals("resources.tasks.schedule", attr.getId());
        Assert.assertFalse(attr.isLazy());
        Assert.assertFalse(attr.isMeta());
        Assert.assertFalse(attr.isLinks());
        Assert.assertTrue(attr.isAssociation());
        Assert.assertNotNull(attr.getOppositeAttribute());
        Assert.assertNotNull("tasks", attr.getOppositeAttribute().getName());
        Assert.assertEquals(Schedule.class, attr.getType().getImplementationClass());

        Assert.assertTrue(attr.isSortable());
        Assert.assertTrue(attr.isFilterable());
        Assert.assertTrue(attr.isInsertable());
        Assert.assertTrue(attr.isUpdatable());
    }

    @Test
    public void testMultiValuedSetRelation() {
        MetaResource meta = resourceProvider.getMeta(Schedule.class);

        MetaResourceField attr = (MetaResourceField) meta.getAttribute("taskSet");
        Assert.assertEquals("taskSet", attr.getName());
        Assert.assertEquals("resources.schedule.taskSet", attr.getId());
        Assert.assertTrue(attr.isLazy());
        Assert.assertFalse(attr.isMeta());
        Assert.assertFalse(attr.isLinks());
        Assert.assertTrue(attr.isAssociation());
        Assert.assertFalse(attr.isOwner());
        Assert.assertNotNull(attr.getOppositeAttribute());
        Assert.assertTrue(attr.getOppositeAttribute().isOwner());
        Assert.assertNotNull("taskSet", attr.getOppositeAttribute().getName());
        Assert.assertEquals(Set.class, attr.getType().getImplementationClass());
        Assert.assertEquals(Task.class, attr.getType().getElementType().getImplementationClass());
        Assert.assertTrue(attr.getType().getClass().getName(), attr.getType() instanceof MetaSetType);

        MetaSetType listType = (MetaSetType) attr.getType();
        Assert.assertTrue(listType.newInstance() instanceof Set);
        Assert.assertTrue(listType.isCollection());
        Assert.assertFalse(listType.isMap());
        Assert.assertNotNull(attr.getType().asCollection());
    }

    @Test
	@Ignore
    public void testMultiValuedListRelation() {
        MetaResource meta = resourceProvider.getMeta(Task.class);

        MetaResourceField attr = (MetaResourceField) meta.getAttribute("projects");
        Assert.assertFalse(attr.isMeta());
        Assert.assertFalse(attr.isLinks());
        Assert.assertTrue(attr.isAssociation());
        Assert.assertTrue(attr.isOwner());
        Assert.assertEquals(ResourceList.class, attr.getType().getImplementationClass());
        Assert.assertEquals(Project.class, attr.getType().getElementType().getImplementationClass());
        Assert.assertTrue(attr.getType() instanceof MetaListType);

        MetaListType listType = (MetaListType) attr.getType();
        Assert.assertTrue(listType.newInstance() instanceof List);
        Assert.assertTrue(listType.isCollection());
        Assert.assertFalse(listType.isMap());
        Assert.assertNotNull(attr.getType().asCollection());
    }


    @Test
    public void testRepository() {
        MetaResource resourceMeta = resourceProvider.getMeta(Schedule.class);
        MetaResourceRepository meta = (MetaResourceRepository) lookup.getMetaById().get(resourceMeta.getId() + "$repository");
        Assert.assertEquals(resourceMeta, meta.getResourceType());
        Assert.assertTrue(meta.isExposed());
        Assert.assertNotNull(meta.getListLinksType());
        Assert.assertNotNull(meta.getListMetaType());
        Assert.assertEquals(ScheduleRepository.ScheduleListLinks.class, meta.getListLinksType().getImplementationClass());
        Assert.assertEquals(ScheduleRepository.ScheduleListMeta.class, meta.getListMetaType().getImplementationClass());

        List<MetaElement> children = new ArrayList<>(meta.getChildren());
        Collections.sort(children, Comparator.comparing(MetaElement::getName));
        Assert.assertEquals(9, children.size());

        MetaResourceAction repositoryActionMeta = (MetaResourceAction) children.get(1);
        Assert.assertEquals("repositoryAction", repositoryActionMeta.getName());
        Assert.assertEquals(MetaRepositoryActionType.REPOSITORY, repositoryActionMeta.getActionType());
        MetaResourceAction resourceActionMeta = (MetaResourceAction) children.get(8);
        Assert.assertEquals("resourceAction", resourceActionMeta.getName());
        Assert.assertEquals(MetaRepositoryActionType.RESOURCE, resourceActionMeta.getActionType());

    }

    @Test
    public void testDynamicResources() {
        for (int i = 0; i < 2; i++) {
            MetaResource meta = (MetaResource) lookup.getMetaById().get("resources.dynamic" + i);
            Assert.assertNotNull(meta);

            MetaAttribute parentAttr = meta.getAttribute("parent");
            Assert.assertNotNull(meta.getAttribute("id"));
            Assert.assertNotNull(meta.getAttribute("value"));
            Assert.assertNotNull(parentAttr);
            Assert.assertNotNull(meta.getAttribute("children"));

            Assert.assertEquals("children", parentAttr.getOppositeAttribute().getName());
            Assert.assertEquals("dynamic" + i, meta.getResourceType());
        }
    }


}
