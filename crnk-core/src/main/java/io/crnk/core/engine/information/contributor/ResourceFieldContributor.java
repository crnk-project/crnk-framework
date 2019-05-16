package io.crnk.core.engine.information.contributor;

import io.crnk.core.engine.information.resource.ResourceField;

import java.util.List;

/**
 * Can be implemented by RelationshipRepository to contribute further (relationship) fields to a resource without
 * modifying its source.
 */
public interface ResourceFieldContributor {

	List<ResourceField> getResourceFields(ResourceFieldContributorContext context);
}
