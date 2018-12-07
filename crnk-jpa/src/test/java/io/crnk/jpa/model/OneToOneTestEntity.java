package io.crnk.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OneToOneTestEntity {

    @Id
    private Long id;

    /**
     * uni-directional
     */
    @OneToOne(fetch = FetchType.LAZY)
    private RelatedEntity oneRelatedValue;

    /**
     * bi-directional
     */
    @OneToOne(fetch = FetchType.LAZY)
    private OneToOneOppositeEntity oppositeValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RelatedEntity getOneRelatedValue() {
        return oneRelatedValue;
    }

    public void setOneRelatedValue(RelatedEntity oneRelatedValue) {
        this.oneRelatedValue = oneRelatedValue;
    }

    public OneToOneOppositeEntity getOppositeValue() {
        return oppositeValue;
    }

    public void setOppositeValue(OneToOneOppositeEntity oppositeValue) {
        this.oppositeValue = oppositeValue;
    }
}
