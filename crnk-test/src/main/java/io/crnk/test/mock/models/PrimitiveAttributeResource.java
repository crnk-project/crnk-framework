package io.crnk.test.mock.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "primitiveAttribute")
public class PrimitiveAttributeResource {

	@JsonApiId
	private Long id;

	private String stringValue;

	private int intValue;

	private long longValue;

	private boolean booleanValue;

	private float floatValue;

	private JsonNode jsonNodeValue;

	private ArrayNode arrayNodeValue;

	private ObjectNode objectNodeValue;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	public JsonNode getJsonNodeValue() {
		return jsonNodeValue;
	}

	public void setJsonNodeValue(JsonNode jsonNodeValue) {
		this.jsonNodeValue = jsonNodeValue;
	}

	public ArrayNode getArrayNodeValue() {
		return arrayNodeValue;
	}

	public void setArrayNodeValue(ArrayNode arrayNodeValue) {
		this.arrayNodeValue = arrayNodeValue;
	}

	public ObjectNode getObjectNodeValue() {
		return objectNodeValue;
	}

	public void setObjectNodeValue(ObjectNode objectNodeValue) {
		this.objectNodeValue = objectNodeValue;
	}
}