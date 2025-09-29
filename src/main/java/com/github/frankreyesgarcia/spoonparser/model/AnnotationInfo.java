package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Represents annotation information extracted from Java code.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnnotationInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("fullyQualifiedName")
    private String fullyQualifiedName;
    
    @JsonProperty("values")
    private Map<String, Object> values;

    // Default constructor for Jackson
    public AnnotationInfo() {}

    public AnnotationInfo(String name, String fullyQualifiedName) {
        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public AnnotationInfo(String name, String fullyQualifiedName, Map<String, Object> values) {
        this.name = name;
        this.fullyQualifiedName = fullyQualifiedName;
        this.values = values;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationInfo that = (AnnotationInfo) o;
        return Objects.equals(fullyQualifiedName, that.fullyQualifiedName) &&
               Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName, values);
    }

    @Override
    public String toString() {
        return "AnnotationInfo{" +
                "name='" + name + '\'' +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", values=" + values +
                '}';
    }
}