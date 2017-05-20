package io.crnk.jpa.model.dto;

import io.crnk.core.resource.annotations.*;

import java.util.List;

@JsonApiResource(type = "testDTO")
public class TestDTO {

	public static final String ATTR_COMPUTED_UPPER_STRING_VALUE = "computedUpperStringValue";

	public static String ATTR_COMPUTED_NUMBER_OF_SMALLER_IDS = "computedNumberOfSmallerIds";

	@JsonApiId
	private Long id;

	private String stringValue;

	private String computedUpperStringValue;

	private long computedNumberOfSmallerIds;

	@JsonApiToOne
	@JsonApiLookupIncludeAutomatically
	private RelatedDTO oneRelatedValue;

	@JsonApiToMany
	@JsonApiLookupIncludeAutomatically
	private List<RelatedDTO> manyRelatedValues;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RelatedDTO getOneRelatedValue() {
		return oneRelatedValue;
	}

	public void setOneRelatedValue(RelatedDTO oneRelatedValue) {
		this.oneRelatedValue = oneRelatedValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getComputedUpperStringValue() {
		return computedUpperStringValue;
	}

	public void setComputedUpperStringValue(String computedUpperStringValue) {
		this.computedUpperStringValue = computedUpperStringValue;
	}

	public long getComputedNumberOfSmallerIds() {
		return computedNumberOfSmallerIds;
	}

	public void setComputedNumberOfSmallerIds(long computedNumberOfSmallerIds) {
		this.computedNumberOfSmallerIds = computedNumberOfSmallerIds;
	}

	public List<RelatedDTO> getManyRelatedValues() {
		return manyRelatedValues;
	}

	public void setManyRelatedValues(List<RelatedDTO> manyRelatedValues) {
		this.manyRelatedValues = manyRelatedValues;
	}
}
