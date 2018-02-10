package io.crnk.core.engine.registry;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;

/**
 * A marker interface that identifies a entry
 */
public interface ResourceEntry {

	ResourceRepositoryInformation getRepositoryInformation();
}
