package com.github.frankreyesgarcia.spoonparser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents extracted information about a Java code element.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElementInfo {
    
    @JsonProperty("fullyQualifiedName")
    private String fullyQualifiedName;
    
    @JsonProperty("type")
    private ElementType type;
    
    @JsonProperty("modifiers")
    private Set<String> modifiers;
    
    @JsonProperty("lineNumber")
    private Integer lineNumber;
    
    @JsonProperty("sourceFile")
    private String sourceFile;
    
    @JsonProperty("annotations")
    private List<AnnotationInfo> annotations;
    
    @JsonProperty("parameters")
    private List<ParameterInfo> parameters;
    
    @JsonProperty("returnType")
    private String returnType;
    
    @JsonProperty("fieldType")
    private String fieldType;
    
    @JsonProperty("superClass")
    private String superClass;
    
    @JsonProperty("interfaces")
    private List<String> interfaces;
    
    @JsonProperty("signature")
    private String signature;

    // Default constructor for Jackson
    public ElementInfo() {}

    public ElementInfo(String fullyQualifiedName, ElementType type, Set<String> modifiers) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.type = type;
        this.modifiers = modifiers;
    }

    // Getters and Setters
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public Set<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(Set<String> modifiers) {
        this.modifiers = modifiers;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public List<AnnotationInfo> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationInfo> annotations) {
        this.annotations = annotations;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementInfo that = (ElementInfo) o;
        return Objects.equals(fullyQualifiedName, that.fullyQualifiedName) &&
               type == that.type &&
               Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName, type, signature);
    }

    @Override
    public String toString() {
        return "ElementInfo{" +
                "fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", type=" + type +
                ", modifiers=" + modifiers +
                ", lineNumber=" + lineNumber +
                ", signature='" + signature + '\'' +
                '}';
    }
}