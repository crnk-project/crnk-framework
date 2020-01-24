package io.crnk.client.inheritance.resources.related;

import java.util.List;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

/*
 * @author syri.
 */
@JsonApiResource(
    type = "related-resource-a-sub-1",
    resourcePath = "related-resource-a"
)
public class RelatedResourceAsub1 extends RelatedResourceA {
    public static final String TYPE = "SUB1";

    @JsonApiRelation
    private List<RelatedResourceB> relatedResourceBS;

    public RelatedResourceAsub1() {
        super(TYPE);
    }

    public List<RelatedResourceB> getRelatedResourceBS() {
        return relatedResourceBS;
    }

    public void setRelatedResourceBS(List<RelatedResourceB> relatedResourceBS) {
        this.relatedResourceBS = relatedResourceBS;
    }
}
