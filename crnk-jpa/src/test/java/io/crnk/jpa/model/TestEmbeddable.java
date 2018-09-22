package io.crnk.jpa.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TestEmbeddable extends TestEmbeddableBase {

	public static final String ATTR_embIntValue = "embIntValue";

	public static final String ATTR_embStringValue = "embStringValue";

	public static final String ATTR_nestedValue = "nestedValue";

	public static final String ATTR_relatedValue = "relatedValue";

	public static final String ATTR_anyValue = "anyValue";


	@Column
	private String embStringValue;

	@Column
	@AttributeOverrides({@AttributeOverride(name = "stringValue", column = @Column(name = "anyStringValue")),
			@AttributeOverride(name = "intValue", column = @Column(name = "anyIntValue")),
			@AttributeOverride(name = "type", column = @Column(name = "anyTypeValue"))})
	private TestAnyType anyValue;

	// @ManyToOne(fetch = FetchType.EAGER)
	// @JoinColumn
	// private RelatedEntity relatedValue;

	@Column
	private TestNestedEmbeddable nestedValue;

	public TestAnyType getAnyValue() {
		return anyValue;
	}

	public void setAnyValue(TestAnyType anyValue) {
		this.anyValue = anyValue;
	}

	//	public RelatedEntity getRelatedValue() {
	//		return relatedValue;
	//	}
	//
	//	public void setRelatedValue(RelatedEntity relatedValue) {
	//		this.relatedValue = relatedValue;
	//	}

	public String getEmbStringValue() {
		return embStringValue;
	}

	public void setEmbStringValue(String embStringValue) {
		this.embStringValue = embStringValue;
	}

	public TestNestedEmbeddable getNestedValue() {
		return nestedValue;
	}

	public void setNestedValue(TestNestedEmbeddable nestedValue) {
		this.nestedValue = nestedValue;
	}
}
