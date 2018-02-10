package io.crnk.core.repository;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.engine.information.resource.ResourceField;

/**
 * specifies to which relationships a relationship repository should be applied to.
 */
public class RelationshipMatcher {

	protected List<RelationshipMatcherRule> rules = new ArrayList<>();

	public RelationshipMatcherRule rule() {
		return new RelationshipMatcherRule(this);
	}

	public boolean matches(ResourceField field) {
		return rules.stream().filter(it -> it.matches(field)).findAny().isPresent();
	}
}
