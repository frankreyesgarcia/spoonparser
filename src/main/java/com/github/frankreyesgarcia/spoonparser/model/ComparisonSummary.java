package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Summary statistics for a comparison between two versions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComparisonSummary {
    
    @JsonProperty("totalBreakingChanges")
    private int totalBreakingChanges;
    
    @JsonProperty("highSeverityChanges")
    private int highSeverityChanges;
    
    @JsonProperty("mediumSeverityChanges")
    private int mediumSeverityChanges;
    
    @JsonProperty("lowSeverityChanges")
    private int lowSeverityChanges;
    
    @JsonProperty("addedElements")
    private int addedElements;
    
    @JsonProperty("removedElements")
    private int removedElements;
    
    @JsonProperty("modifiedElements")
    private int modifiedElements;
    
    @JsonProperty("isBackwardCompatible")
    private boolean isBackwardCompatible;

    // Default constructor for Jackson
    public ComparisonSummary() {}

    // Getters and Setters
    public int getTotalBreakingChanges() {
        return totalBreakingChanges;
    }

    public void setTotalBreakingChanges(int totalBreakingChanges) {
        this.totalBreakingChanges = totalBreakingChanges;
    }

    public int getHighSeverityChanges() {
        return highSeverityChanges;
    }

    public void setHighSeverityChanges(int highSeverityChanges) {
        this.highSeverityChanges = highSeverityChanges;
    }

    public int getMediumSeverityChanges() {
        return mediumSeverityChanges;
    }

    public void setMediumSeverityChanges(int mediumSeverityChanges) {
        this.mediumSeverityChanges = mediumSeverityChanges;
    }

    public int getLowSeverityChanges() {
        return lowSeverityChanges;
    }

    public void setLowSeverityChanges(int lowSeverityChanges) {
        this.lowSeverityChanges = lowSeverityChanges;
    }

    public int getAddedElements() {
        return addedElements;
    }

    public void setAddedElements(int addedElements) {
        this.addedElements = addedElements;
    }

    public int getRemovedElements() {
        return removedElements;
    }

    public void setRemovedElements(int removedElements) {
        this.removedElements = removedElements;
    }

    public int getModifiedElements() {
        return modifiedElements;
    }

    public void setModifiedElements(int modifiedElements) {
        this.modifiedElements = modifiedElements;
    }

    public boolean isBackwardCompatible() {
        return isBackwardCompatible;
    }

    public void setBackwardCompatible(boolean backwardCompatible) {
        isBackwardCompatible = backwardCompatible;
    }

    @Override
    public String toString() {
        return "ComparisonSummary{" +
                "totalBreakingChanges=" + totalBreakingChanges +
                ", highSeverityChanges=" + highSeverityChanges +
                ", addedElements=" + addedElements +
                ", removedElements=" + removedElements +
                ", isBackwardCompatible=" + isBackwardCompatible +
                '}';
    }
}