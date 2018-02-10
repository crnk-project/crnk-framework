package io.crnk.core.repository;

import java.util.Objects;

import io.crnk.core.engine.information.resource.ResourceField;

/**
 * see {@link RelationshipMatcher}
 */
public class RelationshipMatcherRule {

	private final RelationshipMatcher matcher;

	private String sourceResourceType;

	private Class<?> sourceResourceClass;

	private boolean sourceMatchSubTypes;

	private String sourceField;

	private String targetResourceType;

	private Class<?> targetResourceClass;

	private boolean targetMatchSubTypes;

	private String targetField;


	protected RelationshipMatcherRule(RelationshipMatcher matcher) {
		this.matcher = matcher;
	}

	public RelationshipMatcherRule source(String sourceResourceType) {
		this.sourceResourceType = sourceResourceType;
		return this;
	}

	public RelationshipMatcherRule source(Class<?> sourceResourceClass) {
		return source(sourceResourceClass, false);
	}

	public RelationshipMatcherRule source(Class<?> sourceResourceClass, boolean sourceMatchSubTypes) {
		this.sourceResourceClass = sourceResourceClass;
		this.sourceMatchSubTypes = sourceMatchSubTypes;
		return this;
	}

	public RelationshipMatcherRule field(String sourceField) {
		this.sourceField = sourceField;
		return this;
	}

	public RelationshipMatcherRule field(ResourceField sourceField) {
		field(sourceField.getUnderlyingName());
		source(sourceField.getParentResourceInformation().getResourceType());
		return this;
	}

	public RelationshipMatcherRule target(String targetResourceType) {
		this.targetResourceType = targetResourceType;
		return this;
	}

	public RelationshipMatcherRule target(Class<?> targetResourceClass) {
		return target(targetResourceClass, false);
	}

	public RelationshipMatcherRule target(Class<?> targetResourceClass, boolean targetMatchSubTypes) {
		this.targetResourceClass = targetResourceClass;
		this.targetMatchSubTypes = targetMatchSubTypes;
		return this;
	}


	public RelationshipMatcherRule oppositeField(String targetField) {
		this.targetField = targetField;
		return this;
	}

	public RelationshipMatcher add() {
		matcher.rules.add(this);
		return matcher;
	}

	public boolean matches(ResourceField field) {
		boolean matchesSourceType = nullOrMatch(sourceResourceType, field.getParentResourceInformation().getResourceType());
		boolean matchesSourceClass = nullOrMatch(sourceResourceClass, field.getParentResourceInformation().getResourceClass(),
				sourceMatchSubTypes);
		boolean matchesSourceField = nullOrMatch(sourceField, field.getUnderlyingName());
		boolean matchesTargetType = nullOrMatch(targetResourceType, field.getOppositeResourceType());
		boolean matchesTargetClass = nullOrMatch(targetResourceClass, field.getElementType(), targetMatchSubTypes);
		boolean matchesTargetField = nullOrMatch(targetField, field.getOppositeName());
		return matchesSourceType && matchesSourceClass && matchesSourceField && matchesTargetType && matchesTargetClass &&
				matchesTargetField;
	}

	private boolean nullOrMatch(String expected, String actual) {
		return expected == null || Objects.equals(expected, actual);
	}

	private boolean nullOrMatch(Class expected, Class actual, boolean matchSubTypes) {
		return expected == null || Objects.equals(expected, actual) || matchSubTypes && expected.isAssignableFrom(actual);
	}

}
