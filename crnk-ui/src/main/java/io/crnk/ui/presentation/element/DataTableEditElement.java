package io.crnk.ui.presentation.element;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataTableEditElement extends PresentationElement {

    private ObjectNode newResource;

    private String relationshipName;

    private String oppositeRelationshipName;

    private boolean allowDeletions;

    private boolean allowInsertion;

    public ObjectNode getNewResource() {
        return newResource;
    }

    public void setNewResource(ObjectNode newResource) {
        this.newResource = newResource;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public String getOppositeRelationshipName() {
        return oppositeRelationshipName;
    }

    public void setOppositeRelationshipName(String oppositeRelationshipName) {
        this.oppositeRelationshipName = oppositeRelationshipName;
    }

    public boolean isAllowDeletions() {
        return allowDeletions;
    }

    public void setAllowDeletions(boolean allowDeletions) {
        this.allowDeletions = allowDeletions;
    }

    public boolean isAllowInsertion() {
        return allowInsertion;
    }

    public void setAllowInsertion(boolean allowInsertion) {
        this.allowInsertion = allowInsertion;
    }
}
