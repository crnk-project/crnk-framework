package io.crnk.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OneToOneOppositeEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "oppositeValue")
    private OneToOneTestEntity test;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OneToOneTestEntity getTest() {
        return test;
    }

    public void setTest(OneToOneTestEntity test) {
        this.test = test;
    }
}
