package io.crnk.meta;

import java.util.Arrays;

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

	@Test
	public void length() {
		Assert.assertEquals(2, path.length());
	}

	@Test
	public void getLast() {
		Assert.assertEquals(attr2, path.getLast());
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
	public void render() {
		Assert.assertEquals("a.b", path.toString());
	}

	@Test
	public void equals() {
		Assert.assertTrue(path.equals(path));
		Assert.assertFalse(path.equals(new Object()));
	}
}
