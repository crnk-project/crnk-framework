package io.crnk.jpa.model;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import java.util.List;
import java.util.Map;

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
