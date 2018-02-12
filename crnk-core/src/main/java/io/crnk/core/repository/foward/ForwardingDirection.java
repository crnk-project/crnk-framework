package io.crnk.core.repository.foward;

public enum ForwardingDirection {

	/**
	 * Forwards calls to resource repository of resource owning the relationship.
	 */
	OWNER,

	/**
	 * Forwards calls to resource repository of opposite side.
	 */
	OPPOSITE
}
