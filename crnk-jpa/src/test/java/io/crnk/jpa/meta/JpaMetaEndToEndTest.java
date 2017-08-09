package io.crnk.jpa.meta;

import java.io.Serializable;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.jpa.AbstractJpaJerseyTest;
import io.crnk.jpa.model.AnnotationMappedSuperclassEntity;
import io.crnk.jpa.model.AnnotationTestEntity;
import io.crnk.jpa.model.RenamedTestEntity;
import io.crnk.jpa.model.SequenceEntity;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.model.VersionedEntity;
import io.crnk.jpa.model.dto.TestDTO;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaKey;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JpaMetaEndToEndTest extends AbstractJpaJerseyTest {

	@Override
	@Before
	public void setup() {
		super.setup();
	}

	@Test
	public void test() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource testMeta = lookup.getMeta(TestEntity.class, MetaResource.class);
		Assert.assertNotNull(testMeta);
		MetaDataObject superMeta = testMeta.getSuperType();
		Assert.assertEquals(MetaResourceBase.class, superMeta.getClass());

		MetaAttribute embAttrMeta = testMeta.getAttribute(TestEntity.ATTR_embValue);
		Assert.assertEquals(MetaJsonObject.class, embAttrMeta.getType().getClass());
	}

	@Test
	public void testProjectedLob() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource metaResource = lookup.getMeta(AnnotationTestEntity.class, MetaResource.class);
		MetaAttribute lobAttr = metaResource.getAttribute("lobValue");
		Assert.assertTrue(lobAttr.isLob());
	}

	@Test
	public void testRenameResourceType() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource metaResource = lookup.getMeta(RenamedTestEntity.class, MetaResource.class);
		Assert.assertEquals("renamedResource", metaResource.getResourceType());

		RenamedTestEntity test = new RenamedTestEntity();
		test.setId(1L);

		ResourceRepositoryV2<RenamedTestEntity, Serializable> repository = client.getRepositoryForType(RenamedTestEntity.class);
		repository.create(test);
		Assert.assertEquals(1, repository.findAll(new QuerySpec(RenamedTestEntity.class)).size());
		repository.delete(1L);
		Assert.assertEquals(0, repository.findAll(new QuerySpec(RenamedTestEntity.class)).size());
	}

	@Test
	public void testProjectedLobOnMappedSuperclass() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResourceBase metaResource = lookup.getMeta(AnnotationMappedSuperclassEntity.class, MetaResourceBase.class);
		MetaAttribute lobAttr = metaResource.getAttribute("lobValue");
		Assert.assertTrue(lobAttr.isLob());
	}

	@Test
	public void testProjectedColumnAnnotatedValueIsNotNullable() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResourceBase meta = lookup.getMeta(AnnotationTestEntity.class, MetaResourceBase.class);
		MetaAttribute field = meta.getAttribute("notNullableValue");
		Assert.assertFalse(field.isNullable());
	}

	@Test
	public void testProjectedColumnAnnotatedValueIsNullable() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResourceBase meta = lookup.getMeta(AnnotationTestEntity.class, MetaResourceBase.class);
		MetaAttribute field = meta.getAttribute("nullableValue");
		Assert.assertTrue(field.isNullable());
	}

	@Test
	public void testProjectedVersion() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource metaResource = lookup.getMeta(VersionedEntity.class, MetaResource.class);
		MetaAttribute versionAttr = metaResource.getAttribute("version");
		Assert.assertTrue(versionAttr.isVersion());
	}

	@Test
	public void testCascaded() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource meta = lookup.getMeta(TestEntity.class, MetaResource.class);
		MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
		MetaAttribute eagerRelatedAttr = meta.getAttribute("eagerRelatedValue");
		Assert.assertTrue(oneRelatedAttr.isCascaded());
		Assert.assertFalse(eagerRelatedAttr.isCascaded());
	}

	@Test
	public void testAttributeInsertableUpdatable() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource versionMeta = lookup.getMeta(VersionedEntity.class, MetaResource.class);
		MetaAttribute idAttr = versionMeta.getAttribute("id");
		MetaAttribute valueAttr = versionMeta.getAttribute("longValue");
		Assert.assertTrue(idAttr.isInsertable());
		Assert.assertFalse(idAttr.isUpdatable());
		Assert.assertTrue(valueAttr.isInsertable());
		Assert.assertTrue(valueAttr.isUpdatable());

		MetaResourceBase annotationMeta = lookup.getMeta(AnnotationTestEntity.class, MetaResource.class);
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

		MetaResourceBase superMeta = lookup.getMeta(AnnotationMappedSuperclassEntity.class, MetaResourceBase.class);
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
		MetaLookup lookup = metaModule.getLookup();
		MetaResource metaResource = lookup.getMeta(SequenceEntity.class, MetaResource.class);
		Assert.assertTrue(metaResource.getPrimaryKey().isGenerated());
	}

	@Test
	public void testDtoMeta() {
		MetaLookup lookup = metaModule.getLookup();
		MetaResource meta = lookup.getMeta(TestDTO.class, MetaResource.class);
		MetaKey primaryKey = meta.getPrimaryKey();
		Assert.assertNotNull(primaryKey);
		Assert.assertEquals(1, primaryKey.getElements().size());
		Assert.assertEquals("id", primaryKey.getElements().get(0).getName());

		MetaAttribute oneRelatedAttr = meta.getAttribute("oneRelatedValue");
		Assert.assertTrue(oneRelatedAttr.isAssociation());
	}

}
