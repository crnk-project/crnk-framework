package io.crnk.data.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

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
