package io.crnk.data.jpa.model;

import javax.persistence.Entity;

@Entity
public class TestSubclassWithSuperclassGenericsInterface
		extends TestMappedSuperclassWithGenericsInterface<TestSubclassWithSuperclassGenericsInterface> {

}
