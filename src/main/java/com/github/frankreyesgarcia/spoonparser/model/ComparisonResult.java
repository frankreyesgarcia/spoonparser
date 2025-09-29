package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Represents the result of comparing two versions of a codebase.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComparisonResult {
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("oldVersion")
    private String oldVersion;
    
    @JsonProperty("newVersion")
    private String newVersion;
    
    @JsonProperty("breakingChanges")
    private List<BreakingChange> breakingChanges;
    
    @JsonProperty("addedElements")
    private List<ElementInfo> addedElements;
    
    @JsonProperty("removedElements")
    private List<ElementInfo> removedElements;
    
    @JsonProperty("modifiedElements")
    private List<ElementModification> modifiedElements;
    
    @JsonProperty("summary")
    private ComparisonSummary summary;

    // Default constructor for Jackson
    public ComparisonResult() {
        this.timestamp = Instant.now();
    }

    public ComparisonResult(String oldVersion, String newVersion) {
        this();
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    // Getters and Setters
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getOldVersion() {
        return oldVersion;
    }

    public void setOldVersion(String oldVersion) {
        this.oldVersion = oldVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public List<BreakingChange> getBreakingChanges() {
        return breakingChanges;
    }

    public void setBreakingChanges(List<BreakingChange> breakingChanges) {
        this.breakingChanges = breakingChanges;
    }

    public List<ElementInfo> getAddedElements() {
        return addedElements;
    }

    public void setAddedElements(List<ElementInfo> addedElements) {
        this.addedElements = addedElements;
    }

    public List<ElementInfo> getRemovedElements() {
        return removedElements;
    }

    public void setRemovedElements(List<ElementInfo> removedElements) {
        this.removedElements = removedElements;
    }

    public List<ElementModification> getModifiedElements() {
        return modifiedElements;
    }

    public void setModifiedElements(List<ElementModification> modifiedElements) {
        this.modifiedElements = modifiedElements;
    }

    public ComparisonSummary getSummary() {
        return summary;
    }

    public void setSummary(ComparisonSummary summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "ComparisonResult{" +
                "oldVersion='" + oldVersion + '\'' +
                ", newVersion='" + newVersion + '\'' +
                ", breakingChanges=" + (breakingChanges != null ? breakingChanges.size() : 0) +
                ", addedElements=" + (addedElements != null ? addedElements.size() : 0) +
                ", removedElements=" + (removedElements != null ? removedElements.size() : 0) +
                '}';
    }
}