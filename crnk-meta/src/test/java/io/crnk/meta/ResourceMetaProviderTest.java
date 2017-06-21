package io.crnk.meta;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.mock.model.ExtendsBaseResource;
import io.crnk.meta.model.*;
import io.crnk.meta.model.resource.*;
import io.crnk.meta.model.resource.MetaResourceAction.MetaRepositoryActionType;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.*;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.*;

public class ResourceMetaProviderTest extends AbstractMetaTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		super.setup();

		ResourceMetaProvider provider = new ResourceMetaProvider();

		lookup = new MetaLookup();
		lookup.setModuleContext(boot.getModuleRegistry().getContext());
		lookup.addProvider(provider);
		lookup.putIdMapping("io.crnk.test.mock.models", "app");
		lookup.putIdMapping("io.crnk.test.mock.repository", "app");
		lookup.initialize();
	}

	@Test
	public void testReadWriteMethods() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		MetaAttribute idAttr = meta.getAttribute("id");
		Assert.assertEquals("getId", idAttr.getReadMethod().getName());
		Assert.assertEquals("setId", idAttr.getWriteMethod().getName());

		Schedule schedule = new Schedule();
		schedule.setTasksList(new ArrayList<Task>());
		schedule.setId(13L);
		Assert.assertEquals(13L, idAttr.getValue(schedule));

		idAttr.setValue(schedule, 14L);
		Assert.assertEquals(14L, schedule.getId().longValue());

		MetaAttribute listAttr = meta.getAttribute("tasksList");
		Task task = new Task();
		task.setId(12L);
		listAttr.addValue(schedule, task);
		Assert.assertEquals(1, schedule.getTasksList().size());
		listAttr.removeValue(schedule, task);
		Assert.assertEquals(0, schedule.getTasksList().size());
	}

	@Test
	public void getVersionAttribute() {
		// TODO setup versioning concept in json api layer
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		Assert.assertNull(meta.getVersionAttribute());
	}

	@Test
	public void resolvePath() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		MetaAttributePath metaAttributes = meta.resolvePath(Arrays.asList("task", "name"));
		Assert.assertEquals(2, metaAttributes.length());
		Assert.assertEquals("task", metaAttributes.getElement(0).getName());
		Assert.assertEquals("name", metaAttributes.getElement(1).getName());
	}

	@Test
	public void resolvePathWithSubType() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaAttributePath metaAttributes = meta.resolvePath(Arrays.asList("subTypeValue"), true);
		Assert.assertEquals(1, metaAttributes.length());
		Assert.assertEquals("subTypeValue", metaAttributes.getElement(0).getName());
	}


	@Test
	public void getSubTypes() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		List<MetaDataObject> subTypes = meta.getSubTypes(true, false);
		Assert.assertEquals(1, subTypes.size());
		MetaDataObject subType = subTypes.iterator().next();
		Assert.assertEquals(TaskSubType.class, subType.getImplementationClass());
	}


	@Test
	public void getSubTypesOrSelf() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		List<MetaDataObject> subTypes = meta.getSubTypes(true, true);
		Assert.assertEquals(2, subTypes.size());
		Assert.assertTrue(subTypes.contains(meta));
	}

	@Test
	public void testGetAnnotation() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaAttribute idAttr = meta.getAttribute("id");
		Assert.assertNotNull(idAttr.getAnnotation(JsonApiId.class));
	}

	@Test
	public void testGetAnnotations() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaAttribute idAttr = meta.getAttribute("id");
		Assert.assertEquals(1, idAttr.getAnnotations().size());
	}

	@Test
	public void testPreserveAttributeOrder() {
		ResourceInformation resourceInformation =
				boot.getResourceRegistry().getEntryForClass(Schedule.class).getResourceInformation();
		List<ResourceField> fields = resourceInformation.getFields();
		Assert.assertEquals("id", fields.get(0).getUnderlyingName());
		Assert.assertEquals("name", fields.get(1).getUnderlyingName());
		Assert.assertEquals("task", fields.get(2).getUnderlyingName());
		Assert.assertEquals("lazyTask", fields.get(3).getUnderlyingName());
		Assert.assertEquals("tasks", fields.get(4).getUnderlyingName());
		Assert.assertEquals("tasksList", fields.get(5).getUnderlyingName());
		Assert.assertEquals("delayed", fields.get(6).getUnderlyingName());

		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		List<? extends MetaAttribute> attributes = meta.getAttributes();
		Assert.assertEquals("id", attributes.get(0).getName());
		Assert.assertEquals("name", attributes.get(1).getName());
		Assert.assertEquals("task", attributes.get(2).getName());
		Assert.assertEquals("lazyTask", attributes.get(3).getName());
		Assert.assertEquals("tasks", attributes.get(4).getName());
		Assert.assertEquals("tasksList", attributes.get(5).getName());
		Assert.assertEquals("delayed", attributes.get(6).getName());
	}

	@Test
	public void testPrimaryKeyNotNullable() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		MetaAttribute idField = primaryKey.getElements().get(0);
		Assert.assertFalse(idField.isNullable());

		Assert.assertNotNull(idField.getAnnotation(JsonApiId.class));
		Assert.assertEquals(1, idField.getAnnotations().size());
	}


	@Test
	public void testReadMethodAnnotations() {
		MetaResource meta = lookup.getMeta(ReadMethodAnnotatedResource.class, MetaResource.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		MetaAttribute idField = primaryKey.getElements().get(0);
		Assert.assertFalse(idField.isNullable());

		Assert.assertNotNull(idField.getAnnotation(JsonApiId.class));
		Assert.assertEquals(1, idField.getAnnotations().size());
	}

	@Test
	public void testWriteMethodAnnotations() {
		MetaResource meta = lookup.getMeta(WriteMethodAnnotatedResource.class, MetaResource.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		MetaAttribute idField = primaryKey.getElements().get(0);
		Assert.assertFalse(idField.isNullable());

		Assert.assertNotNull(idField.getAnnotation(JsonApiId.class));
		Assert.assertEquals(2, idField.getAnnotations().size()); // nullable + jsonapiid
	}

	@JsonApiResource("readMethodAnnotatedResource")
	class ReadMethodAnnotatedResource {

		private String id;

		@JsonApiId
		public String getId() {
			return id;
		}


		public void setId(String id) {
			this.id = id;
		}
	}


	@JsonApiResource("readMethodAnnotatedResource")
	class WriteMethodAnnotatedResource {

		private String id;

		@JsonApiId
		public String getId() {
			return id;
		}

		@Nullable
		public void setId(String id) {
			this.id = id;
		}
	}


	@Test
	public void testRegularFieldNullable() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaAttribute taskField = meta.getAttribute("task");
		Assert.assertTrue(taskField.isNullable());
	}

	@Test
	public void testPrimitiveFieldNotNullable() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaAttribute taskField = meta.getAttribute("delayed");
		Assert.assertFalse(taskField.isNullable());
	}

	@Test
	public void testInheritance() {
		MetaResource meta = lookup.getMeta(ExtendsBaseResource.class, MetaResource.class);

		Assert.assertNotNull(meta.getAttribute("name"));
		Assert.assertNotNull(meta.getAttribute("baseName"));
		Assert.assertNotNull(meta.getAttribute("id"));

		MetaDataObject superType = meta.getSuperType();
		Assert.assertEquals(MetaResourceBase.class, superType.getClass());
		Assert.assertNotNull(superType.getAttribute("baseName"));
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
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		Assert.assertEquals("schedules", meta.getResourceType());
		Assert.assertEquals("Schedule", meta.getName());
		Assert.assertEquals("app.Schedule", meta.getId());

		Assert.assertEquals(Schedule.class, meta.getImplementationClass());
		Assert.assertEquals(Schedule.class, meta.getImplementationType());
		Assert.assertNull(meta.getParent());
		Assert.assertTrue(meta.getSubTypes().isEmpty());
	}

	@Test
	public void testLinksAttribute() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("linksInformation");
		Assert.assertEquals("linksInformation", attr.getName());
		Assert.assertEquals("app.Task.linksInformation", attr.getId());
		Assert.assertFalse(attr.isLazy());
		Assert.assertFalse(attr.isMeta());
		Assert.assertTrue(attr.isLinks());
		Assert.assertNull(attr.getOppositeAttribute());
		Assert.assertEquals(Task.TaskLinks.class, attr.getType().getImplementationClass());
	}

	@Test
	public void testMetaAttribute() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("metaInformation");
		Assert.assertEquals("metaInformation", attr.getName());
		Assert.assertEquals("app.Task.metaInformation", attr.getId());
		Assert.assertFalse(attr.isLazy());
		Assert.assertTrue(attr.isMeta());
		Assert.assertFalse(attr.isLinks());
		Assert.assertNull(attr.getOppositeAttribute());
		Assert.assertEquals(Task.TaskMeta.class, attr.getType().getImplementationClass());
	}

	@Test
	public void testSingleValuedAttribute() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

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
		MetaResource meta = lookup.getMeta(Project.class, MetaResource.class);

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

		Assert.assertEquals("string$Array", keywordField.getType().getName());
		Assert.assertEquals("base.string$Array", keywordField.getType().getId());

		// FIXME support crnk annotations
		// Assert.assertTrue(dataField.isSortable());
		// Assert.assertTrue(dataField.isFilterable());
	}

	@Test
	public void testMapAttribute() {
		MetaResource meta = lookup.getMeta(Project.class, MetaResource.class);

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
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);
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
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("schedule");
		Assert.assertEquals("schedule", attr.getName());
		Assert.assertEquals("app.Task.schedule", attr.getId());
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
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("tasks");
		Assert.assertEquals("tasks", attr.getName());
		Assert.assertEquals("app.Schedule.tasks", attr.getId());
		Assert.assertTrue(attr.isLazy());
		Assert.assertFalse(attr.isMeta());
		Assert.assertFalse(attr.isLinks());
		Assert.assertTrue(attr.isAssociation());
		Assert.assertNotNull(attr.getOppositeAttribute());
		Assert.assertNotNull("tasks", attr.getOppositeAttribute().getName());
		Assert.assertEquals(Set.class, attr.getType().getImplementationClass());
		Assert.assertEquals(Task.class, attr.getType().getElementType().getImplementationClass());
		Assert.assertTrue(attr.getType() instanceof MetaSetType);

		MetaSetType listType = (MetaSetType) attr.getType();
		Assert.assertTrue(listType.newInstance() instanceof Set);
		Assert.assertTrue(listType.isCollection());
		Assert.assertFalse(listType.isMap());
		Assert.assertNotNull(attr.getType().asCollection());
	}

	@Test
	public void testMultiValuedListRelation() {
		MetaResource meta = lookup.getMeta(Task.class, MetaResource.class);

		MetaResourceField attr = (MetaResourceField) meta.getAttribute("projects");
		Assert.assertFalse(attr.isMeta());
		Assert.assertFalse(attr.isLinks());
		Assert.assertTrue(attr.isAssociation());
		Assert.assertEquals(List.class, attr.getType().getImplementationClass());
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
		MetaResource resourceMeta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaResourceRepository meta = (MetaResourceRepository) lookup.getMetaById().get(resourceMeta.getId() + "Repository");
		Assert.assertEquals(resourceMeta, meta.getResourceType());
		Assert.assertNotNull(meta.getListLinksType());
		Assert.assertNotNull(meta.getListMetaType());
		Assert.assertEquals(ScheduleRepository.ScheduleListLinks.class, meta.getListLinksType().getImplementationClass());
		Assert.assertEquals(ScheduleRepository.ScheduleListMeta.class, meta.getListMetaType().getImplementationClass());

		List<MetaElement> children = new ArrayList<>(meta.getChildren());
		Collections.sort(children, new Comparator<MetaElement>() {

			@Override
			public int compare(MetaElement o1, MetaElement o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		Assert.assertEquals(4, children.size());

		MetaResourceAction repositoryActionMeta = (MetaResourceAction) children.get(0);
		Assert.assertEquals("repositoryAction", repositoryActionMeta.getName());
		Assert.assertEquals(MetaRepositoryActionType.REPOSITORY, repositoryActionMeta.getActionType());
		MetaResourceAction resourceActionMeta = (MetaResourceAction) children.get(3);
		Assert.assertEquals("resourceAction", resourceActionMeta.getName());
		Assert.assertEquals(MetaRepositoryActionType.RESOURCE, resourceActionMeta.getActionType());

	}

}
