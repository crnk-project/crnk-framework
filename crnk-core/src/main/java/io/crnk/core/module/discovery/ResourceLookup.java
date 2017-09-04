package io.crnk.core.module.discovery;

import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.legacy.repository.ResourceRepository;

import java.util.Set;

public interface ResourceLookup {

	Set<Class<?>> getResourceClasses();

}
