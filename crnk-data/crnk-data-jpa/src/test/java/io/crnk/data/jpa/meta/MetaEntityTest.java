package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.model.SequenceEntity;
import io.crnk.data.jpa.model.TestSubclassWithSuperclassGenerics;
import io.crnk.data.jpa.model.TestSubclassWithSuperclassPk;
import io.crnk.data.jpa.model.TestMappedSuperclassWithPk;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaPrimaryKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class MetaEntityTest {

    private JpaMetaProvider metaProvider;

    @Before
    public void setup() {
        metaProvider = new JpaMetaProvider(Collections.emptySet());
        MetaLookupImpl lookup = new MetaLookupImpl();
        lookup.addProvider(metaProvider);
    }

    @Test
    public void testPrimaryKeyOnParentMappedSuperClass() {

        MetaEntity meta = metaProvider.discoverMeta(TestSubclassWithSuperclassPk.class);
        MetaPrimaryKey primaryKey = meta.getPrimaryKey();
        Assert.assertNotNull(primaryKey);
        Assert.assertEquals(1, primaryKey.getElements().size());
        Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
        Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
        Assert.assertFalse(primaryKey.isGenerated());
    }

    @Test
    public void testPrimaryKeyOnMappedSuperClass() {
        MetaMappedSuperclass meta = metaProvider.discoverMeta(TestMappedSuperclassWithPk.class);
        MetaPrimaryKey primaryKey = meta.getPrimaryKey();
        Assert.assertNotNull(primaryKey);
        Assert.assertEquals(1, primaryKey.getElements().size());
        Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
        Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
        Assert.assertFalse(primaryKey.isGenerated());
    }

    @Test
    public void testGeneratedPrimaryKey() {
        MetaDataObject meta = metaProvider.discoverMeta(SequenceEntity.class).asDataObject();
        MetaPrimaryKey primaryKey = meta.getPrimaryKey();
        Assert.assertNotNull(primaryKey);
        Assert.assertEquals(1, primaryKey.getElements().size());
        Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
        Assert.assertTrue(primaryKey.isGenerated());
    }

    @Test
    public void testGenericsOnParentMappedSuperClass() {
        MetaEntity meta = metaProvider.discoverMeta(TestSubclassWithSuperclassGenerics.class);
        MetaPrimaryKey primaryKey = meta.getPrimaryKey();
        Assert.assertNotNull(primaryKey);
        Assert.assertEquals(1, primaryKey.getElements().size());
        Assert.assertEquals("id", primaryKey.getElements().get(0).getName());
        Assert.assertTrue(primaryKey.getElements().get(0).isPrimaryKeyAttribute());
        Assert.assertFalse(primaryKey.isGenerated());
        MetaAttribute genericAttr = meta.getAttribute("generic");
        Assert.assertNotNull(genericAttr);
        Assert.assertEquals(Integer.class, genericAttr.getType().getImplementationClass());

        MetaAttribute genericListAttr = meta.getAttribute("genericList");
        Assert.assertNotNull(genericListAttr);
        Assert.assertEquals(List.class, genericListAttr.getType().getImplementationClass());
        Assert.assertEquals(String.class, genericListAttr.getType().getElementType().getImplementationClass());

        MetaAttribute genericListList = meta.getAttribute("genericListSuper");
        Assert.assertNotNull(genericListList);
        Assert.assertEquals(List.class, genericListList.getType().getImplementationClass());
        Assert.assertEquals(String.class, genericListList.getType().getElementType().getImplementationClass());

        Stream<? extends MetaAttribute> stream = meta.getAttributes().stream();
        Assert.assertEquals(1, stream.filter(it -> it.getName().endsWith("genericListSuper")).count());
    }
}
