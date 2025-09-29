package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Represents parameter information for methods and constructors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParameterInfo {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("fullyQualifiedType")
    private String fullyQualifiedType;
    
    @JsonProperty("annotations")
    private List<AnnotationInfo> annotations;
    
    @JsonProperty("isFinal")
    private boolean isFinal;
    
    @JsonProperty("isVarArgs")
    private boolean isVarArgs;

    // Default constructor for Jackson
    public ParameterInfo() {}

    public ParameterInfo(String name, String type, String fullyQualifiedType) {
        this.name = name;
        this.type = type;
        this.fullyQualifiedType = fullyQualifiedType;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullyQualifiedType() {
        return fullyQualifiedType;
    }

    public void setFullyQualifiedType(String fullyQualifiedType) {
        this.fullyQualifiedType = fullyQualifiedType;
    }

    public List<AnnotationInfo> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationInfo> annotations) {
        this.annotations = annotations;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isVarArgs() {
        return isVarArgs;
    }

    public void setVarArgs(boolean varArgs) {
        isVarArgs = varArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterInfo that = (ParameterInfo) o;
        return isFinal == that.isFinal &&
               isVarArgs == that.isVarArgs &&
               Objects.equals(name, that.name) &&
               Objects.equals(fullyQualifiedType, that.fullyQualifiedType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fullyQualifiedType, isFinal, isVarArgs);
    }

    @Override
    public String toString() {
        return "ParameterInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", fullyQualifiedType='" + fullyQualifiedType + '\'' +
                ", isFinal=" + isFinal +
                ", isVarArgs=" + isVarArgs +
                '}';
    }
}