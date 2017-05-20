package io.crnk.meta;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.meta.mock.model.ExtendsBaseResource;
import io.crnk.meta.mock.model.Schedule;
import io.crnk.meta.mock.model.ScheduleRepository.ScheduleListLinks;
import io.crnk.meta.mock.model.ScheduleRepository.ScheduleListMeta;
import io.crnk.meta.mock.model.Task;
import io.crnk.meta.mock.model.Task.TaskLinksInformation;
import io.crnk.meta.mock.model.Task.TaskMetaInformation;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.resource.*;
import io.crnk.meta.model.resource.MetaResourceAction.MetaRepositoryActionType;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResourceMetaProviderTest extends AbstractMetaTest {

	private MetaLookup lookup;

	@Before
	public void setup() {
		super.setup();

		ResourceMetaProvider provider = new ResourceMetaProvider();

		lookup = new MetaLookup();
		lookup.setModuleContext(boot.getModuleRegistry().getContext());
		lookup.addProvider(provider);
		lookup.putIdMapping("io.crnk.meta.mock.model", "app");
		lookup.initialize();
	}

	@Test
	public void testPrimaryKey() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);

		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull("id", primaryKey.getName());
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
		Assert.assertSame(primaryKey.getElements().get(0), meta.getAttribute("id"));
		Assert.assertTrue(meta.getPrimaryKey().isUnique());

		MetaAttribute idField = primaryKey.getElements().get(0);
		Assert.assertTrue(idField.isSortable());
		Assert.assertTrue(idField.isFilterable());
		Assert.assertTrue(idField.isInsertable());
		Assert.assertFalse(idField.isUpdatable());
		Assert.assertTrue(idField.isPrimaryKeyAttribute());
	}

	@Test
	public void testPreserveAttributeOrder() {
		ResourceInformation resourceInformation = boot.getResourceRegistry().getEntryForClass(Schedule.class).getResourceInformation();
		List<ResourceField> fields = resourceInformation.getFields();
		Assert.assertEquals("id", fields.get(0).getUnderlyingName());
		Assert.assertEquals("name", fields.get(1).getUnderlyingName());
		Assert.assertEquals("task", fields.get(2).getUnderlyingName());
		Assert.assertEquals("lazyTask", fields.get(3).getUnderlyingName());
		Assert.assertEquals("tasks", fields.get(4).getUnderlyingName());
		Assert.assertEquals("delayed", fields.get(5).getUnderlyingName());

		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		List<? extends MetaAttribute> attributes = meta.getAttributes();
		Assert.assertEquals("id", attributes.get(0).getName());
		Assert.assertEquals("name", attributes.get(1).getName());
		Assert.assertEquals("task", attributes.get(2).getName());
		Assert.assertEquals("lazyTask", attributes.get(3).getName());
		Assert.assertEquals("tasks", attributes.get(4).getName());
		Assert.assertEquals("delayed", attributes.get(5).getName());
	}

	@Test
	public void testPrimaryKeyNotNullable() {
		MetaResource meta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		MetaAttribute idField = primaryKey.getElements().get(0);
		Assert.assertFalse(idField.isNullable());
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
		Assert.assertEquals(TaskLinksInformation.class, attr.getType().getImplementationClass());
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
		Assert.assertEquals(TaskMetaInformation.class, attr.getType().getImplementationClass());
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
	public void testMultiValuedRelation() {
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
		Assert.assertEquals(List.class, attr.getType().getImplementationClass());
		Assert.assertEquals(Task.class, attr.getType().getElementType().getImplementationClass());
	}

	@Test
	public void testRepository() {
		MetaResource resourceMeta = lookup.getMeta(Schedule.class, MetaResource.class);
		MetaResourceRepository meta = (MetaResourceRepository) lookup.getMetaById().get(resourceMeta.getId() + "Repository");
		Assert.assertEquals(resourceMeta, meta.getResourceType());
		Assert.assertNotNull(meta.getListLinksType());
		Assert.assertNotNull(meta.getListMetaType());
		Assert.assertEquals(ScheduleListLinks.class, meta.getListLinksType().getImplementationClass());
		Assert.assertEquals(ScheduleListMeta.class, meta.getListMetaType().getImplementationClass());

		List<MetaElement> children = new ArrayList<>(meta.getChildren());
		Collections.sort(children, new Comparator<MetaElement>() {

			@Override
			public int compare(MetaElement o1, MetaElement o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		Assert.assertEquals(2, children.size());

		MetaResourceAction repositoryActionMeta = (MetaResourceAction) children.get(0);
		Assert.assertEquals("repositoryAction", repositoryActionMeta.getName());
		Assert.assertEquals(MetaRepositoryActionType.REPOSITORY, repositoryActionMeta.getActionType());
		MetaResourceAction resourceActionMeta = (MetaResourceAction) children.get(1);
		Assert.assertEquals("resourceAction", resourceActionMeta.getName());
		Assert.assertEquals(MetaRepositoryActionType.RESOURCE, resourceActionMeta.getActionType());

	}

}
