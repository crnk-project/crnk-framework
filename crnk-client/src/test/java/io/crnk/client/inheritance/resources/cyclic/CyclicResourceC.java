package io.crnk.client.inheritance.resources.cyclic;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

/*
 * @author syri.
 */
@JsonApiResource(
    type = "cyclic-resource-c"
)
public class CyclicResourceC {
    @JsonApiId
    private long id;

    @JsonApiRelation
    private CyclicResourceA cyclicResourceA;

    @JsonApiRelation
    private CyclicResourceB cyclicResourceB;

    public CyclicResourceC(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CyclicResourceA getCyclicResourceA() {
        return cyclicResourceA;
    }

    public void setCyclicResourceA(CyclicResourceA cyclicResourceA) {
        this.cyclicResourceA = cyclicResourceA;
    }

    public CyclicResourceB getCyclicResourceB() {
        return cyclicResourceB;
    }

    public void setCyclicResourceB(CyclicResourceB cyclicResourceB) {
        this.cyclicResourceB = cyclicResourceB;
    }
}
