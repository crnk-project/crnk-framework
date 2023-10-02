package io.crnk.data.jpa.model;

import jakarta.persistence.Entity;

@Entity
public class TestSubclassWithSuperclassGenericsInterface
		extends TestMappedSuperclassWithGenericsInterface<TestSubclassWithSuperclassGenericsInterface> {

}
