package io.crnk.data.jpa.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
