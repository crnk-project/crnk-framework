package io.crnk.data.jpa.model;

public interface TestInterfaceWithGenerics<T> {

	T getGeneric();

	void setGeneric(T generic);
}
