package io.crnk.core.repository;

import java.io.Serializable;

public interface UntypedResourceRepository<T, I > extends ResourceRepository<T, I> {

	String getResourceType();

}