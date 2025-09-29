package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a breaking change detected between two versions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BreakingChange {
    
    @JsonProperty("type")
    private BreakingChangeType type;
    
    @JsonProperty("elementFqn")
    private String elementFqn;
    
    @JsonProperty("elementType")
    private ElementType elementType;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("severity")
    private Severity severity;
    
    @JsonProperty("oldValue")
    private String oldValue;
    
    @JsonProperty("newValue")
    private String newValue;
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    @JsonProperty("lineNumber")
    private Integer lineNumber;

    // Default constructor for Jackson
    public BreakingChange() {}

    public BreakingChange(BreakingChangeType type, String elementFqn, ElementType elementType, String description) {
        this.type = type;
        this.elementFqn = elementFqn;
        this.elementType = elementType;
        this.description = description;
        this.severity = determineSeverity(type);
    }

    private Severity determineSeverity(BreakingChangeType type) {
        switch (type) {
            case REMOVED_PUBLIC_METHOD:
            case REMOVED_PUBLIC_FIELD:
            case REMOVED_PUBLIC_CLASS:
            case CHANGED_METHOD_SIGNATURE:
                return Severity.HIGH;
            case VISIBILITY_REDUCED:
            case MODIFIER_CHANGED:
                return Severity.MEDIUM;
            case ANNOTATION_CHANGED:
                return Severity.LOW;
            default:
                return Severity.MEDIUM;
        }
    }

    // Getters and Setters
    public BreakingChangeType getType() {
        return type;
    }

    public void setType(BreakingChangeType type) {
        this.type = type;
        this.severity = determineSeverity(type);
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "BreakingChange{" +
                "type=" + type +
                ", elementFqn='" + elementFqn + '\'' +
                ", severity=" + severity +
                ", description='" + description + '\'' +
                '}';
    }

    public enum BreakingChangeType {
        REMOVED_PUBLIC_METHOD,
        REMOVED_PUBLIC_FIELD,
        REMOVED_PUBLIC_CLASS,
        CHANGED_METHOD_SIGNATURE,
        CHANGED_RETURN_TYPE,
        VISIBILITY_REDUCED,
        MODIFIER_CHANGED,
        ANNOTATION_CHANGED,
        INHERITANCE_CHANGED
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}