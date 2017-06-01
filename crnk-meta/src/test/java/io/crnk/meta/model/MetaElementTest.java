package io.crnk.meta.model;

import io.crnk.meta.AbstractMetaTest;
import org.junit.Test;

public class MetaElementTest extends AbstractMetaTest {


	@Test(expected = IllegalStateException.class)
	public void checkDataObjectCast() {
		new MetaKey().asDataObject();
	}

	@Test(expected = IllegalStateException.class)
	public void checkTypeCast() {
		new MetaKey().asType();
	}
}
