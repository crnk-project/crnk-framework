package io.crnk.meta;

import java.util.Arrays;
import java.util.Iterator;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MetaAttributePathTest {

	private MetaAttributePath path;

	private MetaAttribute attr1;

	private MetaAttribute attr2;

	@Before
	public void setup() {
		attr1 = Mockito.mock(MetaAttribute.class);
		attr2 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr1.getName()).thenReturn("a");
		Mockito.when(attr2.getName()).thenReturn("b");

		path = new MetaAttributePath(Arrays.asList(attr1, attr2));
	}


	@Test(expected = IllegalArgumentException.class)
	public void invalidConstructorArgumentsThrowsException() {
		new MetaAttributePath((MetaAttribute[]) null);
	}


	@Test
	public void concat() {
		MetaAttribute attr3 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr3.getName()).thenReturn("c");
		Assert.assertEquals("a.b.c", path.concat(attr3).toString());
	}


	@Test
	public void toStringForEmptyPath() {
		Assert.assertEquals("", MetaAttributePath.EMPTY_PATH.toString());
	}

	@Test
	public void toStringForSingleAttributePath() {
		MetaAttribute attr3 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr3.getName()).thenReturn("c");
		path = new MetaAttributePath(Arrays.asList(attr3));
		Assert.assertEquals("c", path.toString());
	}


	@Test
	public void testHashCode() {
		MetaAttribute attr3 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr3.getName()).thenReturn("c");
		MetaAttributePath path2 = new MetaAttributePath(Arrays.asList(attr3));
		MetaAttributePath path3 = new MetaAttributePath(Arrays.asList(attr3));

		Assert.assertNotEquals(path2.hashCode(), path.hashCode());
		Assert.assertEquals(path2.hashCode(), path3.hashCode());
	}


	@Test
	public void length() {
		Assert.assertEquals(2, path.length());
	}

	@Test
	public void getLast() {
		Assert.assertEquals(attr2, path.getLast());
	}


	@Test
	public void iterator() {
		Iterator<MetaAttribute> iterator = path.iterator();
		Assert.assertTrue(iterator.hasNext());
		Assert.assertEquals("a", iterator.next().getName());
		Assert.assertEquals("b", iterator.next().getName());
		Assert.assertFalse(iterator.hasNext());
	}


	@Test
	public void getLastForEmptyPath() {
		Assert.assertNull(MetaAttributePath.EMPTY_PATH.getLast());
	}


	@Test
	public void getElement() {
		Assert.assertEquals(attr1, path.getElement(0));
		Assert.assertEquals(attr2, path.getElement(1));
	}


	@Test
	public void subPath() {
		MetaAttributePath subPath = path.subPath(1);
		Assert.assertEquals(1, subPath.length());
		Assert.assertEquals(attr2, subPath.getElement(0));
	}

	@Test
	public void subRangePath() {
		MetaAttributePath subPath = path.subPath(1, 2);
		Assert.assertEquals(1, subPath.length());
		Assert.assertEquals(attr2, subPath.getElement(0));
	}


	@Test
	public void render() {
		Assert.assertEquals("a.b", path.toString());
	}

	@Test
	public void equals() {
		Assert.assertTrue(path.equals(path));
		Assert.assertFalse(path.equals(new Object()));
	}
}
