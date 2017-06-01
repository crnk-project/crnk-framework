package io.crnk.core.engine.document;

import java.util.Arrays;

import io.crnk.core.utils.Nullable;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DocumentTest {

	@Test
	public void testDocumentEqualsContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(Document.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void getCollectionData() {
		Document doc = new Document();
		Assert.assertFalse(doc.getCollectionData().isPresent());

		doc.setData(Nullable.nullValue());
		Assert.assertTrue(doc.getCollectionData().get().isEmpty());

		Resource resource1 = Mockito.mock(Resource.class);
		doc.setData(Nullable.of((Object) resource1));
		Assert.assertEquals(1, doc.getCollectionData().get().size());

		Resource resource2 = Mockito.mock(Resource.class);
		doc.setData(Nullable.of((Object) Arrays.asList(resource1, resource2)));
		Assert.assertEquals(2, doc.getCollectionData().get().size());

	}
}
