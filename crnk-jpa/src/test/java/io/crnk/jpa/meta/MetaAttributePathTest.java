package io.crnk.jpa.meta;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaAttributePath;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class MetaAttributePathTest {

	@Test
	public void render() {
		MetaAttribute attr1 = Mockito.mock(MetaAttribute.class);
		MetaAttribute attr2 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr1.getName()).thenReturn("a");
		Mockito.when(attr2.getName()).thenReturn("b");

		MetaAttributePath path = new MetaAttributePath(Arrays.asList(attr1, attr2));
		Assert.assertEquals("a.b", path.toString());
	}

	@Test
	public void equals() {
		MetaAttribute attr1 = Mockito.mock(MetaAttribute.class);
		MetaAttribute attr2 = Mockito.mock(MetaAttribute.class);
		MetaAttributePath path = new MetaAttributePath(Arrays.asList(attr1, attr2));
		Assert.assertTrue(path.equals(path));
		Assert.assertFalse(path.equals(new Object()));
	}
}
