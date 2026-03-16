package io.crnk.data.jpa.model;

import javax.persistence.ElementCollection;
import javax.persistence.MappedSuperclass;
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
