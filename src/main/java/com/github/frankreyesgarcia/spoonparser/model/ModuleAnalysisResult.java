package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents analysis results for a single Maven module.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleAnalysisResult {
    
    @JsonProperty("moduleName")
    private String moduleName;
    
    @JsonProperty("modulePath")
    private String modulePath;
    
    @JsonProperty("elements")
    private List<ElementInfo> elements;
    
    @JsonProperty("sourceFiles")
    private List<String> sourceFiles;
    
    @JsonProperty("dependencies")
    private List<String> dependencies;
    
    @JsonProperty("compilationErrors")
    private List<String> compilationErrors;

    // Default constructor for Jackson
    public ModuleAnalysisResult() {}

    public ModuleAnalysisResult(String moduleName, String modulePath) {
        this.moduleName = moduleName;
        this.modulePath = modulePath;
    }

    // Getters and Setters
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public List<ElementInfo> getElements() {
        return elements;
    }

    public void setElements(List<ElementInfo> elements) {
        this.elements = elements;
    }

    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(List<String> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getCompilationErrors() {
        return compilationErrors;
    }

    public void setCompilationErrors(List<String> compilationErrors) {
        this.compilationErrors = compilationErrors;
    }

    @Override
    public String toString() {
        return "ModuleAnalysisResult{" +
                "moduleName='" + moduleName + '\'' +
                ", modulePath='" + modulePath + '\'' +
                ", elements=" + (elements != null ? elements.size() : 0) +
                '}';
    }
}