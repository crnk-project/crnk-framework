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
	public void testResourceEqualsContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(Relationship.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void serializeArray() throws IOException {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of((Object) Arrays.asList(new ResourceIdentifier("a", "b"))));
		checkSerialize(relationship);
	}

	@Test
	public void serializeSingleData() throws IOException {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of((Object) new ResourceIdentifier("a", "b")));
		checkSerialize(relationship);
	}

	@Test
	public void serializeNull() throws IOException {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.nullValue());
		checkSerialize(relationship);
	}

	@Test
	public void getCollectionDataReturnsListForSingleElement() throws IOException {
		Relationship relationship = new Relationship();
		ResourceIdentifier id = new ResourceIdentifier("a", "b");
		relationship.setData(Nullable.of((Object) id));
		Assert.assertEquals(Arrays.asList(id), relationship.getCollectionData().get());
	}

	@Test(expected = IllegalStateException.class)
	public void setInvalidDataThrowsException() throws IOException {
		Relationship relationship = new Relationship();
		relationship.setData(Nullable.of((Object) "not a ResourceIdentifier"));
	}


	private void checkSerialize(Relationship relationship) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(JacksonModule.createJacksonModule());
		String json = mapper.writeValueAsString(relationship);
		Relationship copy = mapper.readerFor(Relationship.class).readValue(json);
		Assert.assertEquals(relationship, copy);
	}
}
