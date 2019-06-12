package io.crnk.client.inheritance.resources.cyclic;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

/*
 * @author syri.
 */
@JsonApiResource(type = "cyclic-resource-b")
public class CyclicResourceB {
    @JsonApiId
    private long id;

    @JsonApiRelation
    private CyclicResourceASub1 resourceASub1;

    @JsonApiRelation
    private CyclicResourceC cyclicResourceC;

    public CyclicResourceB() {
    }

    public CyclicResourceB(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public CyclicResourceASub1 getResourceASub1() {
        return resourceASub1;
    }

    public void setResourceASub1(CyclicResourceASub1 resourceASub1) {
        this.resourceASub1 = resourceASub1;
    }

    public CyclicResourceC getCyclicResourceC() {
        return cyclicResourceC;
    }

    public void setCyclicResourceC(CyclicResourceC cyclicResourceC) {
        this.cyclicResourceC = cyclicResourceC;
    }
}
