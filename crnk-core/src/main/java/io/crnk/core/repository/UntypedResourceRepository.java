package io.crnk.core.repository;

import java.io.Serializable;

public interface UntypedResourceRepository<T, I extends Serializable> extends ResourceRepositoryV2<T, I> {

	String getResourceType();

}