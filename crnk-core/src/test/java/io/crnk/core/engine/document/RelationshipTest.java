package io.crnk.core.engine.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.utils.Nullable;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class RelationshipTest {

	@Test
	public void testResourceEqualsContract() {
		EqualsVerifier.forClass(Relationship.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void serializeArray() throws IOException {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(Arrays.asList(new ResourceIdentifier("a", "b"))));
		checkSerialize(relationship);
	}

	@Test
	public void serializeSingleData() throws IOException {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of(new ResourceIdentifier("a", "b")));
		checkSerialize(relationship);
	}

	@Test
	public void serializeNull() throws IOException {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.nullValue());
		checkSerialize(relationship);
	}

	@Test
	public void getCollectionDataReturnsListForSingleElement() {
		Relationship relationship = new Relationship();
		ResourceIdentifier id = new ResourceIdentifier("a", "b");
		relationship.setData(Nullable.of(id));
		Assert.assertEquals(Arrays.asList(id), relationship.getCollectionData().get());
	}

	@Test(expected = IllegalStateException.class)
	public void setInvalidDataThrowsException() {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of("not a ResourceIdentifier"));
	}


	private void checkSerialize(Relationship relationship) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule());
		String json = mapper.writeValueAsString(relationship);
		Relationship copy = mapper.readerFor(Relationship.class).readValue(json);
		Assert.assertEquals(relationship, copy);
	}
}
