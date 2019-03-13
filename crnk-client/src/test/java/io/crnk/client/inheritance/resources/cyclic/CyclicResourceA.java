package io.crnk.client.inheritance.resources.cyclic;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

/*
 * @author syri.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CyclicResourceASub1.class, name = CyclicResourceASub1.TYPE),
})
@JsonApiResource(
    type = "cyclic-resource-a",
    subTypes = {
        CyclicResourceASub1.class,
    }
)
@JsonPropertyOrder({"type"})
public abstract class CyclicResourceA {
    @JsonApiId
    private long id;

    protected String type;

    @JsonApiRelation
    private CyclicResourceC cyclicResourceC;

    public CyclicResourceA(String type) {
        this.type = type;
    }

    public CyclicResourceA(String type, long id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CyclicResourceC getCyclicResourceC() {
        return cyclicResourceC;
    }

    public void setCyclicResourceC(CyclicResourceC cyclicResourceC) {
        this.cyclicResourceC = cyclicResourceC;
    }
}
