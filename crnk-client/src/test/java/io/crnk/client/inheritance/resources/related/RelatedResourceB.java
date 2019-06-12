package io.crnk.client.inheritance.resources.related;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

/*
 * @author syri.
 */
@JsonApiResource(type = "related-resource-b")
public class RelatedResourceB {
    @JsonApiId
    private long id;

    @JsonApiRelation
    private RelatedResourceAsub1 relatedResourceAsub1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RelatedResourceAsub1 getRelatedResourceAsub1() {
        return relatedResourceAsub1;
    }

    public void setRelatedResourceAsub1(RelatedResourceAsub1 relatedResourceAsub1) {
        this.relatedResourceAsub1 = relatedResourceAsub1;
    }
}
