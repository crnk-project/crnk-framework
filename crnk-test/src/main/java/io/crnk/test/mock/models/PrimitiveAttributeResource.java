package io.crnk.test.mock.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

import java.util.Date;
import java.util.UUID;

@JsonApiResource(type = "primitiveAttribute")
public class PrimitiveAttributeResource {

	@JsonApiId
	private Long id;

	private String stringValue;

	private int intValue;

	private long longValue;

	private boolean booleanValue;

	private float floatValue;

	private short shortValue;

	private double doubleValue;

	private JsonNode jsonNodeValue;

	private ArrayNode arrayNodeValue;

	private ObjectNode objectNodeValue;

	private Long nullableLongValue;

	private Boolean nullableBooleanValue;

	private Byte nullableByteValue;

	private Short nullableShortValue;

	private Integer nullableIntegerValue;

	private Float nullableFloatValue;

	private Double nullableDoubleValue;

	private UUID uuidValue;

	private Date dateValue;

	private Object objectValue;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getUuidValue() {
		return uuidValue;
	}

	public void setUuidValue(UUID uuidValue) {
		this.uuidValue = uuidValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public Object getObjectValue() {
		return objectValue;
	}

	public void setObjectValue(Object objectValue) {
		this.objectValue = objectValue;
	}

	public short getShortValue() {
		return shortValue;
	}

	public void setShortValue(short shortValue) {
		this.shortValue = shortValue;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Long getNullableLongValue() {
		return nullableLongValue;
	}

	public void setNullableLongValue(Long nullableLongValue) {
		this.nullableLongValue = nullableLongValue;
	}

	public Boolean getNullableBooleanValue() {
		return nullableBooleanValue;
	}

	public void setNullableBooleanValue(Boolean nullableBooleanValue) {
		this.nullableBooleanValue = nullableBooleanValue;
	}

	public Byte getNullableByteValue() {
		return nullableByteValue;
	}

	public void setNullableByteValue(Byte nullableByteValue) {
		this.nullableByteValue = nullableByteValue;
	}

	public Short getNullableShortValue() {
		return nullableShortValue;
	}

	public void setNullableShortValue(Short nullableShortValue) {
		this.nullableShortValue = nullableShortValue;
	}

	public Integer getNullableIntegerValue() {
		return nullableIntegerValue;
	}

	public void setNullableIntegerValue(Integer nullableIntegerValue) {
		this.nullableIntegerValue = nullableIntegerValue;
	}

	public Float getNullableFloatValue() {
		return nullableFloatValue;
	}

	public void setNullableFloatValue(Float nullableFloatValue) {
		this.nullableFloatValue = nullableFloatValue;
	}

	public Double getNullableDoubleValue() {
		return nullableDoubleValue;
	}

	public void setNullableDoubleValue(Double nullableDoubleValue) {
		this.nullableDoubleValue = nullableDoubleValue;
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