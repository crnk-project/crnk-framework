package io.crnk.core.engine.registry;

/**
 * Identifies a relationship repository entry
 */
@Deprecated
public interface ResponseRelationshipEntry {


	/**
	 * @return target resource type. null to accept any target
	 */
	String getTargetResourceType();
}
