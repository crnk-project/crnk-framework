package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.internal.query.MetaComputedAttribute;
import org.junit.Test;

public class MetaComputedAttributeTest {


	@Test(expected = UnsupportedOperationException.class)
	public void getValueNotSupported() {
		MetaComputedAttribute attr = new MetaComputedAttribute();
		attr.getValue(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setValueNotSupported() {
		MetaComputedAttribute attr = new MetaComputedAttribute();
		attr.setValue(null, null);
	}
}
