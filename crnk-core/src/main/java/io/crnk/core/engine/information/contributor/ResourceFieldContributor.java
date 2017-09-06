package io.crnk.core.engine.information.contributor;

import java.util.List;

import io.crnk.core.engine.information.resource.ResourceField;

/**
 * Can be implemented by RelationshipRepositoryV2 to contribute further (relationship) fields to a resource without
 * modifying its source.
 */
public interface ResourceFieldContributor {

	List<ResourceField> getResourceFields(ResourceFieldContributorContext context);
}
