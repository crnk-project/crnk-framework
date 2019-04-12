package io.crnk.core.repository;

import java.io.Serializable;

/**
 * @Deprecated make use of RelationshipRepository.getMatcher()
 */
@Deprecated
public interface UntypedRelationshipRepository<T, I , D, J >
		extends RelationshipRepository<T, I, D, J> {

	String getSourceResourceType();

	String getTargetResourceType();

}