package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a modification to an existing element between versions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElementModification {
    
    @JsonProperty("elementFqn")
    private String elementFqn;
    
    @JsonProperty("elementType")
    private ElementType elementType;
    
    @JsonProperty("oldElement")
    private ElementInfo oldElement;
    
    @JsonProperty("newElement")
    private ElementInfo newElement;
    
    @JsonProperty("changes")
    private List<String> changes;
    
    @JsonProperty("isBreaking")
    private boolean isBreaking;

    // Default constructor for Jackson
    public ElementModification() {}

    public ElementModification(String elementFqn, ElementType elementType, ElementInfo oldElement, ElementInfo newElement) {
        this.elementFqn = elementFqn;
        this.elementType = elementType;
        this.oldElement = oldElement;
        this.newElement = newElement;
    }

    // Getters and Setters
    public String getElementFqn() {
        return elementFqn;
    }

    public void setElementFqn(String elementFqn) {
        this.elementFqn = elementFqn;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public ElementInfo getOldElement() {
        return oldElement;
    }

    public void setOldElement(ElementInfo oldElement) {
        this.oldElement = oldElement;
    }

    public ElementInfo getNewElement() {
        return newElement;
    }

    public void setNewElement(ElementInfo newElement) {
        this.newElement = newElement;
    }

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    public boolean isBreaking() {
        return isBreaking;
    }

    public void setBreaking(boolean breaking) {
        isBreaking = breaking;
    }

    @Override
    public String toString() {
        return "ElementModification{" +
                "elementFqn='" + elementFqn + '\'' +
                ", elementType=" + elementType +
                ", isBreaking=" + isBreaking +
                ", changes=" + (changes != null ? changes.size() : 0) +
                '}';
    }
}