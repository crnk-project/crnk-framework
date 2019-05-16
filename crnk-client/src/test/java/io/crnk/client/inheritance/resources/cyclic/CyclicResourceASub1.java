package io.crnk.client.inheritance.resources.cyclic;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

/*
 * @author syri.
 */
@JsonApiResource(
    type = "cyclic-resource-a-sub-1",
    resourcePath = "cyclic-resource-a"
)
public class CyclicResourceASub1 extends CyclicResourceA {
    public static final String TYPE = "SUB1";

    @JsonApiRelation
    private CyclicResourceB cyclicResourceB;

    public CyclicResourceASub1() {
        super(TYPE);
    }

    public CyclicResourceASub1(long id) {
        super(TYPE, id);
    }

    public CyclicResourceB getCyclicResourceB() {
        return cyclicResourceB;
    }

    public void setCyclicResourceB(CyclicResourceB cyclicResourceB) {
        this.cyclicResourceB = cyclicResourceB;
    }
}
