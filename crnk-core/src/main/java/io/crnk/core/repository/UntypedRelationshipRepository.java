package io.crnk.core.repository;

import java.io.Serializable;

/**
 * @Deprecated make use of RelationshipRepositoryV2.getMatcher()
 */
@Deprecated
public interface UntypedRelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		extends RelationshipRepositoryV2<T, I, D, J> {

	String getSourceResourceType();

	String getTargetResourceType();

}