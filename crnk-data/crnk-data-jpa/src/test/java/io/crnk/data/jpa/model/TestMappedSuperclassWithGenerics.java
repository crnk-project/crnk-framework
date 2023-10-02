package io.crnk.data.jpa.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.MappedSuperclass;
import java.util.List;

@MappedSuperclass
public class TestMappedSuperclassWithGenerics<T> extends TestMappedSupersuperclassWithGenerics<Integer, T> {
	@ElementCollection
	private List<T> genericList;

	public TestMappedSuperclassWithGenerics() {}

	public List<T> getGenericList() {
		return genericList;
	}

	public void setGenericList(List<T> genericList) {
		this.genericList = genericList;
	}
}
