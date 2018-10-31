package io.crnk.data;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.jackson.ErrorDataDeserializer;
import io.crnk.core.engine.internal.jackson.ErrorDataSerializer;
import io.crnk.core.utils.Nullable;
import io.crnk.format.plainjson.internal.PlainJsonDocument;
import io.crnk.format.plainjson.internal.PlainJsonDocumentDeserializer;
import io.crnk.format.plainjson.internal.PlainJsonDocumentSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PlainTextSerializerTest {


	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(new PlainJsonDocumentSerializer());
		simpleModule.addDeserializer(PlainJsonDocument.class, new PlainJsonDocumentDeserializer(objectMapper));
		simpleModule.addSerializer(new ErrorDataSerializer());
		simpleModule.addDeserializer(ErrorData.class, new ErrorDataDeserializer());
		objectMapper.registerModule(simpleModule);
	}

	@Test
	public void emptyDocument() throws IOException {
		PlainJsonDocument document = new PlainJsonDocument();
		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);

		Assert.assertNull(copy.getMeta());
		Assert.assertNull(copy.getLinks());
		Assert.assertNull(copy.getErrors());
		Assert.assertNull(copy.getIncluded());
		Assert.assertFalse(copy.getData().isPresent());
	}

	@Test
	public void meta() throws IOException {
		ObjectNode meta = (ObjectNode) objectMapper.readTree("{\"a\": \"b\"}");

		PlainJsonDocument document = new PlainJsonDocument();
		document.setMeta(meta);

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);
		Assert.assertEquals(meta, copy.getMeta());
	}

	@Test
	public void links() throws IOException {
		ObjectNode links = (ObjectNode) objectMapper.readTree("{\"a\": \"b\"}");

		PlainJsonDocument document = new PlainJsonDocument();
		document.setLinks(links);

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);
		Assert.assertEquals(links, copy.getLinks());
	}

	@Test
	public void errors() throws IOException {
		ErrorDataBuilder builder = new ErrorDataBuilder();
		builder.setStatus("test");
		ErrorData errorData = builder.build();

		PlainJsonDocument document = new PlainJsonDocument();
		document.setErrors(Arrays.asList(errorData));

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);
		Assert.assertEquals(document.getErrors(), copy.getErrors());
	}


	@Test
	public void resource() throws IOException {
		ObjectNode attrValue = (ObjectNode) objectMapper.readTree("{\"a\": \"b\"}");
		ObjectNode someMeta = (ObjectNode) objectMapper.readTree("{\"someMeta\": \"1\"}");
		ObjectNode someLinks = (ObjectNode) objectMapper.readTree("{\"someLink\": \"2\"}");

		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(new ResourceIdentifier("a", "b")));
		relationship.setLinks(someLinks);
		relationship.setMeta(someMeta);

		Resource resource = new Resource();
		resource.setId("someId");
		resource.setType("someType");
		resource.setMeta(someMeta);
		resource.setLinks(someLinks);
		resource.getAttributes().put("someAttr", attrValue);
		resource.getRelationships().put("someRelation", relationship);

		ErrorDataBuilder builder = new ErrorDataBuilder();
		builder.setStatus("test");

		PlainJsonDocument document = new PlainJsonDocument();
		document.setData(Nullable.of(resource));

		String json = objectMapper.writeValueAsString(document);
		PlainJsonDocument copy = objectMapper.readValue(json, PlainJsonDocument.class);

		Resource resourceCopy = copy.getSingleData().get();
		Assert.assertEquals(resource.getId(), resourceCopy.getId());
		Assert.assertEquals(resource.getType(), resourceCopy.getType());
		Assert.assertEquals(resource.getMeta(), resourceCopy.getMeta());
		Assert.assertEquals(resource.getLinks(), resourceCopy.getLinks());
		Assert.assertEquals(resource.getAttributes(), resourceCopy.getAttributes());
		Assert.assertEquals(resource.getRelationships(), resourceCopy.getRelationships());
	}
}
