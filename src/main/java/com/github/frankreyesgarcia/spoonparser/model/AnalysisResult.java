package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a code analysis operation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResult {
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("projectPath")
    private String projectPath;
    
    @JsonProperty("moduleResults")
    private Map<String, ModuleAnalysisResult> moduleResults;
    
    @JsonProperty("elements")
    private List<ElementInfo> elements;
    
    @JsonProperty("totalElements")
    private int totalElements;
    
    @JsonProperty("analysisTimeMs")
    private long analysisTimeMs;
    
    @JsonProperty("errors")
    private List<String> errors;

    // Default constructor for Jackson
    public AnalysisResult() {
        this.timestamp = Instant.now();
    }

    public AnalysisResult(String projectPath) {
        this();
        this.projectPath = projectPath;
    }

    // Getters and Setters
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public Map<String, ModuleAnalysisResult> getModuleResults() {
        return moduleResults;
    }

    public void setModuleResults(Map<String, ModuleAnalysisResult> moduleResults) {
        this.moduleResults = moduleResults;
    }

    public List<ElementInfo> getElements() {
        return elements;
    }

    public void setElements(List<ElementInfo> elements) {
        this.elements = elements;
        this.totalElements = elements != null ? elements.size() : 0;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }

    public void setAnalysisTimeMs(long analysisTimeMs) {
        this.analysisTimeMs = analysisTimeMs;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "timestamp=" + timestamp +
                ", projectPath='" + projectPath + '\'' +
                ", totalElements=" + totalElements +
                ", analysisTimeMs=" + analysisTimeMs +
                '}';
    }
}