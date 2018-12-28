package io.crnk.core.repository;

public interface UntypedResourceRepository<T, I> extends ResourceRepository<T, I> {

    String getResourceType();

}