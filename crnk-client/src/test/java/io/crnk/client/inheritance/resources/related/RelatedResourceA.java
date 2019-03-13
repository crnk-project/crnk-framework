package io.crnk.client.inheritance.resources.related;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.crnk.core.resource.annotations.JsonApiId;
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
    @JsonSubTypes.Type(value = RelatedResourceAsub1.class, name = RelatedResourceAsub1.TYPE),
})
@JsonApiResource(
    type = "related-resource-a",
    subTypes = {
        RelatedResourceAsub1.class,
    }
)
@JsonPropertyOrder({"type"})
public class RelatedResourceA {

    private String type;

    @JsonApiId
    private long id;

    public RelatedResourceA(String type) {
        this.type = type;
    }

    public RelatedResourceA(String type, long id) {
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
}
