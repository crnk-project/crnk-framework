package io.crnk.core.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class ResourceEqualsContractTest {

	@Test
	public void testResourceIdEqualsContract() {
		EqualsVerifier.forClass(ResourceIdentifier.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void testResourceEqualsContract() {
		ObjectNode redNode = JsonNodeFactory.instance.objectNode().put("color", "red");
		ObjectNode blueNode = JsonNodeFactory.instance.objectNode().put("color", "blue");
		EqualsVerifier.forClass(Resource.class)
				.usingGetClass()
				.suppress(Warning.NONFINAL_FIELDS)
				.withPrefabValues(ObjectNode.class, redNode, blueNode)
				.withPrefabValues(JsonNode.class, redNode, blueNode)
				.verify();
	}

}
