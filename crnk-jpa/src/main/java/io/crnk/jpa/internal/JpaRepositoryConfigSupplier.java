package io.crnk.jpa.internal;

import io.crnk.jpa.JpaRepositoryConfig;

/**
 * Contract towards JpaModule to access configuration. Avoids issues with proxies like the ones created by CDI/Weld that cannot expose class methods.
 */
public interface JpaRepositoryConfigSupplier {

	JpaRepositoryConfig getRepositoryConfig();
}
