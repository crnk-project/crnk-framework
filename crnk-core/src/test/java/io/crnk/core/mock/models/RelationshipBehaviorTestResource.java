package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;

@JsonApiResource(type = "relationshipBehaviorTest")
public class RelationshipBehaviorTestResource {

    @JsonApiId
    private Long id;

    private String name;

    @JsonApiRelationId
    private Long testRelationIdId;

    @JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
    private Schedule testRelationId;


    @JsonApiRelation(repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
    private Project testImplicityFromOwner;

    @JsonApiRelation(opposite = "test", repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_GET_OPPOSITE_SET_OWNER)
    private Task testImplicitGetOppositeModifyOwner;

    @JsonApiRelation(lookUp = LookupIncludeBehavior.NONE)
    private LazyTask testNoLookup;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTestRelationIdId(Long testRelationIdId) {
        this.testRelationIdId = testRelationIdId;
        this.testRelationId = null;
    }

    public Schedule getTestRelationId() {
        return testRelationId;
    }

    public void setTestRelationId(Schedule testRelationId) {
        this.testRelationId = testRelationId;
        this.testRelationIdId = testRelationId != null ? testRelationId.getId() : null;
    }

    public Long getTestRelationIdId() {
        return testRelationIdId;
    }

    public Project getTestImplicityFromOwner() {
        return testImplicityFromOwner;
    }

    public void setTestImplicityFromOwner(Project testImplicityFromOwner) {
        this.testImplicityFromOwner = testImplicityFromOwner;
    }

    public Task getTestImplicitGetOppositeModifyOwner() {
        return testImplicitGetOppositeModifyOwner;
    }

    public void setTestImplicitGetOppositeModifyOwner(Task testImplicitGetOppositeModifyOwner) {
        this.testImplicitGetOppositeModifyOwner = testImplicitGetOppositeModifyOwner;
    }

    public LazyTask getTestNoLookup() {
        return testNoLookup;
    }

    public void setTestNoLookup(LazyTask testNoLookup) {
        this.testNoLookup = testNoLookup;
    }

}
