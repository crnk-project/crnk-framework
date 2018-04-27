package io.crnk.core.engine.document;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

	@Test
	public void checkJsonApiServerInfoNotSerializedIfNull() throws JsonProcessingException {
		Document document = new Document();
		document.setJsonapi(null);
		Assert.assertNull(document.getJsonapi());
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter writer = objectMapper.writerFor(Document.class);
		String json = writer.writeValueAsString(document);
		Assert.assertEquals("{}", json);
	}

	@Test
	public void checkJsonApiServerInfoSerialized() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter writer = objectMapper.writerFor(Document.class);

		ObjectNode info = (ObjectNode) objectMapper.readTree("{\"a\" : \"b\"}");
		Document document = new Document();
		document.setJsonapi(info);
		Assert.assertSame(info, document.getJsonapi());

		String json = writer.writeValueAsString(document);
		Assert.assertEquals("{\"jsonapi\":{\"a\":\"b\"}}", json);
	}
}
