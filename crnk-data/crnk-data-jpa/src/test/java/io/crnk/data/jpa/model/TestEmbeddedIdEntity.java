package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class TestEmbeddedIdEntity {

    public static final String ATTR_id = "id";

    public static final String ATTR_longValue = "longValue";

    @EmbeddedId
    private TestIdEmbeddable id;

    @Column
    private long longValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private TestEntity testEntity;

    public TestIdEmbeddable getId() {
        return id;
    }

    public void setId(TestIdEmbeddable id) {
        this.id = id;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public TestEntity getTestEntity() {
        return testEntity;
    }

    public void setTestEntity(TestEntity testEntity) {
        this.testEntity = testEntity;
    }
}
