package io.crnk.jpa.meta;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.model.AnnotationMappedSubtypeEntity;
import io.crnk.jpa.model.AnnotationTestEntity;
import io.crnk.jpa.model.RenamedTestEntity;
import io.crnk.jpa.model.SequenceEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.model.VersionedEntity;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

public class JpaMetaEndToEndTest extends AbstractJpaJerseyTest {

	@Override
	@Before
	public void setup() {
		super.setup();
	}

	@Test
	public void test() {
		MetaResource testMeta = resourceMetaProvider.getMeta(TestEntity.class);
		Assert.assertNotNull(testMeta);

		MetaAttribute embAttrMeta = testMeta.getAttribute(TestEntity.ATTR_embValue);
		Assert.assertEquals(MetaJsonObject.class, embAttrMeta.getType().getClass());
	}

	@Test
	public void testProjectedLob() {
		MetaResource metaResource = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
		MetaAttribute lobAttr = metaResource.getAttribute("lobValue");
		Assert.assertTrue(lobAttr.isLob());
	}

	@Test
	public void testRenameResourceType() {
		MetaResource metaResource = resourceMetaProvider.getMeta(RenamedTestEntity.class);
		Assert.assertEquals("renamedResource", metaResource.getResourceType());

		RenamedTestEntity test = new RenamedTestEntity();
		test.setId(1L);

		ResourceRepository<RenamedTestEntity, Serializable> repository = client.getRepositoryForType(RenamedTestEntity.class);
		repository.create(test);
		Assert.assertEquals(1, repository.findAll(new QuerySpec(RenamedTestEntity.class)).size());
		repository.delete(1L);
		Assert.assertEquals(0, repository.findAll(new QuerySpec(RenamedTestEntity.class)).size());
	}

	@Test
	public void testProjectedLobOnMappedSuperclass() {
		MetaResourceBase metaResource = resourceMetaProvider.getMeta(AnnotationMappedSubtypeEntity.class);
		MetaAttribute lobAttr = metaResource.getAttribute("lobValue");
		Assert.assertTrue(lobAttr.isLob());
	}

	@Test
	public void testProjectedColumnAnnotatedValueIsNotNullable() {
		MetaResourceBase meta = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("notNullableValue");
		Assert.assertFalse(field.isNullable());
	}

	@Test
	public void testProjectedColumnAnnotatedValueIsNullable() {
		MetaResourceBase meta = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
		MetaAttribute field = meta.getAttribute("nullableValue");
		Assert.assertTrue(field.isNullable());
	}

	@Test
	public void testProjectedVersion() {
		MetaResource metaResource = resourceMetaProvider.getMeta(VersionedEntity.class);
		MetaAttribute versionAttr = metaResource.getAttribute("version");
		Assert.assertTrue(versionAttr.isVersion());
	}

	@Test
	public void testCascaded() {
		MetaResource meta = resourceMetaProvider.getMeta(TestEntity.class);
		MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
		MetaAttribute eagerRelatedAttr = meta.getAttribute("eagerRelatedValue");
		Assert.assertTrue(oneRelatedAttr.isCascaded());
		Assert.assertFalse(eagerRelatedAttr.isCascaded());
	}

	@Test
	public void testAttributeInsertableUpdatable() {
		MetaResource versionMeta = resourceMetaProvider.getMeta(VersionedEntity.class);
		MetaAttribute idAttr = versionMeta.getAttribute("id");
		MetaAttribute valueAttr = versionMeta.getAttribute("longValue");
		Assert.assertTrue(idAttr.isInsertable());
		Assert.assertFalse(idAttr.isUpdatable());
		Assert.assertTrue(valueAttr.isInsertable());
		Assert.assertTrue(valueAttr.isUpdatable());

		MetaResourceBase annotationMeta = resourceMetaProvider.getMeta(AnnotationTestEntity.class);
		MetaAttribute fieldAnnotatedAttr = annotationMeta.getAttribute("fieldAnnotatedValue");
		MetaAttribute columnAnnotatedAttr = annotationMeta.getAttribute("columnAnnotatedValue");
		Assert.assertTrue(fieldAnnotatedAttr.isInsertable());
		Assert.assertFalse(fieldAnnotatedAttr.isUpdatable());
		Assert.assertFalse(fieldAnnotatedAttr.isSortable());
		Assert.assertFalse(fieldAnnotatedAttr.isFilterable());
		Assert.assertFalse(columnAnnotatedAttr.isInsertable());
		Assert.assertTrue(columnAnnotatedAttr.isUpdatable());
		Assert.assertTrue(columnAnnotatedAttr.isSortable());
		Assert.assertTrue(columnAnnotatedAttr.isFilterable());

		MetaAttribute embeddableValueAttr = annotationMeta.getAttribute("embeddableValue");
		Assert.assertFalse(embeddableValueAttr.isInsertable());
		Assert.assertTrue(embeddableValueAttr.isUpdatable());
		Assert.assertTrue(embeddableValueAttr.isSortable());
		Assert.assertFalse(embeddableValueAttr.isFilterable());

		MetaResourceBase superMeta = resourceMetaProvider.getMeta(AnnotationMappedSubtypeEntity.class);
		fieldAnnotatedAttr = superMeta.getAttribute("fieldAnnotatedValue");
		columnAnnotatedAttr = superMeta.getAttribute("columnAnnotatedValue");
		MetaAttribute lobAttr = superMeta.getAttribute("lobValue");
		Assert.assertTrue(fieldAnnotatedAttr.isInsertable());
		Assert.assertFalse(fieldAnnotatedAttr.isUpdatable());
		Assert.assertFalse(fieldAnnotatedAttr.isSortable());
		Assert.assertFalse(fieldAnnotatedAttr.isFilterable());
		Assert.assertFalse(columnAnnotatedAttr.isInsertable());
		Assert.assertTrue(columnAnnotatedAttr.isUpdatable());
		Assert.assertTrue(columnAnnotatedAttr.isSortable());
		Assert.assertTrue(columnAnnotatedAttr.isFilterable());
		Assert.assertTrue(lobAttr.isInsertable());
		Assert.assertTrue(lobAttr.isUpdatable());
		Assert.assertFalse(lobAttr.isSortable());
		Assert.assertFalse(lobAttr.isFilterable());

	}

	@Test
	public void testProjectedSequencePrimaryKey() {
		MetaResource metaResource = resourceMetaProvider.getMeta(SequenceEntity.class);
		Assert.assertTrue(metaResource.getPrimaryKey().isGenerated());
	}
}
