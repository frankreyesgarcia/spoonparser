package com.github.frankreyesgarcia.spoonparser.comparator;

import com.github.frankreyesgarcia.spoonparser.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares two versions of analyzed code to detect breaking changes and compatibility issues.
 */
public class VersionComparator {
    
    private static final Logger logger = LoggerFactory.getLogger(VersionComparator.class);
    
    /**
     * Compare two analysis results to detect breaking changes.
     */
    public ComparisonResult compare(AnalysisResult oldVersion, AnalysisResult newVersion) {
        return compare(oldVersion, newVersion, "old", "new");
    }
    
    /**
     * Compare two analysis results with version labels.
     */
    public ComparisonResult compare(AnalysisResult oldVersion, AnalysisResult newVersion, 
                                   String oldVersionLabel, String newVersionLabel) {
        
        logger.info("Comparing versions: {} -> {}", oldVersionLabel, newVersionLabel);
        
        ComparisonResult result = new ComparisonResult(oldVersionLabel, newVersionLabel);
        
        // Create maps for quick lookup
        Map<String, ElementInfo> oldElements = createElementMap(oldVersion.getElements());
        Map<String, ElementInfo> newElements = createElementMap(newVersion.getElements());
        
        // Find added, removed, and modified elements
        List<ElementInfo> addedElements = findAddedElements(oldElements, newElements);
        List<ElementInfo> removedElements = findRemovedElements(oldElements, newElements);
        List<ElementModification> modifiedElements = findModifiedElements(oldElements, newElements);
        
        // Detect breaking changes
        List<BreakingChange> breakingChanges = detectBreakingChanges(removedElements, modifiedElements);
        
        // Create summary
        ComparisonSummary summary = createSummary(breakingChanges, addedElements, removedElements, modifiedElements);
        
        // Set results
        result.setAddedElements(addedElements);
        result.setRemovedElements(removedElements);
        result.setModifiedElements(modifiedElements);
        result.setBreakingChanges(breakingChanges);
        result.setSummary(summary);
        
        logger.info("Comparison completed. Found {} breaking changes, {} added, {} removed, {} modified", 
                   breakingChanges.size(), addedElements.size(), removedElements.size(), modifiedElements.size());
        
        return result;
    }
    
    private Map<String, ElementInfo> createElementMap(List<ElementInfo> elements) {
        if (elements == null) {
            return new HashMap<>();
        }
        
        return elements.stream()
                .collect(Collectors.toMap(
                    this::getElementKey,
                    element -> element,
                    (existing, replacement) -> existing // Handle duplicates by keeping the first
                ));
    }
    
    private String getElementKey(ElementInfo element) {
        // Use FQN + signature for unique identification
        String key = element.getFullyQualifiedName();
        if (element.getSignature() != null) {
            key += ":" + element.getSignature();
        }
        return key;
    }
    
    private List<ElementInfo> findAddedElements(Map<String, ElementInfo> oldElements, 
                                               Map<String, ElementInfo> newElements) {
        return newElements.entrySet().stream()
                .filter(entry -> !oldElements.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
    
    private List<ElementInfo> findRemovedElements(Map<String, ElementInfo> oldElements, 
                                                 Map<String, ElementInfo> newElements) {
        return oldElements.entrySet().stream()
                .filter(entry -> !newElements.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
    
    private List<ElementModification> findModifiedElements(Map<String, ElementInfo> oldElements, 
                                                          Map<String, ElementInfo> newElements) {
        List<ElementModification> modifications = new ArrayList<>();
        
        for (Map.Entry<String, ElementInfo> entry : oldElements.entrySet()) {
            String key = entry.getKey();
            ElementInfo oldElement = entry.getValue();
            ElementInfo newElement = newElements.get(key);
            
            if (newElement != null && !areElementsEqual(oldElement, newElement)) {
                ElementModification modification = new ElementModification(
                    oldElement.getFullyQualifiedName(),
                    oldElement.getType(),
                    oldElement,
                    newElement
                );
                
                List<String> changes = detectChanges(oldElement, newElement);
                modification.setChanges(changes);
                modification.setBreaking(isBreakingModification(oldElement, newElement, changes));
                
                modifications.add(modification);
            }
        }
        
        return modifications;
    }
    
    private boolean areElementsEqual(ElementInfo old, ElementInfo newElement) {
        // Quick structural comparison
        if (!Objects.equals(old.getType(), newElement.getType())) return false;
        if (!Objects.equals(old.getModifiers(), newElement.getModifiers())) return false;
        if (!Objects.equals(old.getReturnType(), newElement.getReturnType())) return false;
        if (!Objects.equals(old.getFieldType(), newElement.getFieldType())) return false;
        if (!Objects.equals(old.getSignature(), newElement.getSignature())) return false;
        
        // Compare parameters
        if (!areParametersEqual(old.getParameters(), newElement.getParameters())) return false;
        
        // Compare annotations
        if (!areAnnotationsEqual(old.getAnnotations(), newElement.getAnnotations())) return false;
        
        return true;
    }
    
    private boolean areParametersEqual(List<ParameterInfo> oldParams, List<ParameterInfo> newParams) {
        if (oldParams == null && newParams == null) return true;
        if (oldParams == null || newParams == null) return false;
        if (oldParams.size() != newParams.size()) return false;
        
        for (int i = 0; i < oldParams.size(); i++) {
            ParameterInfo oldParam = oldParams.get(i);
            ParameterInfo newParam = newParams.get(i);
            
            if (!Objects.equals(oldParam.getFullyQualifiedType(), newParam.getFullyQualifiedType())) return false;
            if (oldParam.isFinal() != newParam.isFinal()) return false;
            if (oldParam.isVarArgs() != newParam.isVarArgs()) return false;
        }
        
        return true;
    }
    
    private boolean areAnnotationsEqual(List<AnnotationInfo> oldAnns, List<AnnotationInfo> newAnns) {
        if (oldAnns == null && newAnns == null) return true;
        if (oldAnns == null || newAnns == null) return false;
        if (oldAnns.size() != newAnns.size()) return false;
        
        // Create sets for comparison (order doesn't matter for annotations)
        Set<String> oldAnnKeys = oldAnns.stream()
                .map(ann -> ann.getFullyQualifiedName() + ":" + ann.getValues())
                .collect(Collectors.toSet());
        
        Set<String> newAnnKeys = newAnns.stream()
                .map(ann -> ann.getFullyQualifiedName() + ":" + ann.getValues())
                .collect(Collectors.toSet());
        
        return oldAnnKeys.equals(newAnnKeys);
    }
    
    private List<String> detectChanges(ElementInfo oldElement, ElementInfo newElement) {
        List<String> changes = new ArrayList<>();
        
        // Check modifiers
        if (!Objects.equals(oldElement.getModifiers(), newElement.getModifiers())) {
            changes.add("Modifiers changed from " + oldElement.getModifiers() + " to " + newElement.getModifiers());
        }
        
        // Check return type
        if (!Objects.equals(oldElement.getReturnType(), newElement.getReturnType())) {
            changes.add("Return type changed from " + oldElement.getReturnType() + " to " + newElement.getReturnType());
        }
        
        // Check field type
        if (!Objects.equals(oldElement.getFieldType(), newElement.getFieldType())) {
            changes.add("Field type changed from " + oldElement.getFieldType() + " to " + newElement.getFieldType());
        }
        
        // Check parameters
        if (!areParametersEqual(oldElement.getParameters(), newElement.getParameters())) {
            changes.add("Parameters changed");
        }
        
        // Check annotations
        if (!areAnnotationsEqual(oldElement.getAnnotations(), newElement.getAnnotations())) {
            changes.add("Annotations changed");
        }
        
        return changes;
    }
    
    private boolean isBreakingModification(ElementInfo oldElement, ElementInfo newElement, List<String> changes) {
        // Check if any change is breaking
        
        // Visibility reduction is always breaking for public elements
        if (isVisibilityReduced(oldElement, newElement)) {
            return true;
        }
        
        // Method signature changes are breaking
        if (oldElement.getType() == ElementType.METHOD || oldElement.getType() == ElementType.CONSTRUCTOR) {
            if (!Objects.equals(oldElement.getSignature(), newElement.getSignature())) {
                return true;
            }
        }
        
        // Return type changes are breaking
        if (!Objects.equals(oldElement.getReturnType(), newElement.getReturnType())) {
            return true;
        }
        
        // Field type changes are breaking
        if (!Objects.equals(oldElement.getFieldType(), newElement.getFieldType())) {
            return true;
        }
        
        return false;
    }
    
    private boolean isVisibilityReduced(ElementInfo oldElement, ElementInfo newElement) {
        int oldVisibility = getVisibilityLevel(oldElement.getModifiers());
        int newVisibility = getVisibilityLevel(newElement.getModifiers());
        return newVisibility < oldVisibility;
    }
    
    private int getVisibilityLevel(Set<String> modifiers) {
        if (modifiers == null) return 1; // package-private
        
        if (modifiers.contains("PUBLIC")) return 3;
        if (modifiers.contains("PROTECTED")) return 2;
        if (modifiers.contains("PRIVATE")) return 0;
        return 1; // package-private
    }
    
    private List<BreakingChange> detectBreakingChanges(List<ElementInfo> removedElements, 
                                                      List<ElementModification> modifiedElements) {
        List<BreakingChange> breakingChanges = new ArrayList<>();
        
        // Removed elements are breaking changes if they were public
        for (ElementInfo removed : removedElements) {
            if (isPublicElement(removed)) {
                BreakingChange change = createBreakingChangeForRemoval(removed);
                breakingChanges.add(change);
            }
        }
        
        // Modified elements that are breaking
        for (ElementModification modification : modifiedElements) {
            if (modification.isBreaking()) {
                BreakingChange change = createBreakingChangeForModification(modification);
                breakingChanges.add(change);
            }
        }
        
        return breakingChanges;
    }
    
    private boolean isPublicElement(ElementInfo element) {
        return element.getModifiers() != null && element.getModifiers().contains("PUBLIC");
    }
    
    private BreakingChange createBreakingChangeForRemoval(ElementInfo removed) {
        BreakingChange.BreakingChangeType changeType;
        
        switch (removed.getType()) {
            case METHOD:
                changeType = BreakingChange.BreakingChangeType.REMOVED_PUBLIC_METHOD;
                break;
            case FIELD:
                changeType = BreakingChange.BreakingChangeType.REMOVED_PUBLIC_FIELD;
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION:
                changeType = BreakingChange.BreakingChangeType.REMOVED_PUBLIC_CLASS;
                break;
            default:
                changeType = BreakingChange.BreakingChangeType.REMOVED_PUBLIC_METHOD;
        }
        
        BreakingChange change = new BreakingChange(
            changeType,
            removed.getFullyQualifiedName(),
            removed.getType(),
            "Removed public " + removed.getType().toString().toLowerCase() + ": " + removed.getFullyQualifiedName()
        );
        
        change.setSourceFile(removed.getSourceFile());
        change.setLineNumber(removed.getLineNumber());
        
        return change;
    }
    
    private BreakingChange createBreakingChangeForModification(ElementModification modification) {
        ElementInfo oldElement = modification.getOldElement();
        ElementInfo newElement = modification.getNewElement();
        
        BreakingChange.BreakingChangeType changeType = determineModificationChangeType(oldElement, newElement);
        
        BreakingChange change = new BreakingChange(
            changeType,
            modification.getElementFqn(),
            modification.getElementType(),
            "Modified " + modification.getElementType().toString().toLowerCase() + " with breaking changes"
        );
        
        change.setSourceFile(newElement.getSourceFile());
        change.setLineNumber(newElement.getLineNumber());
        
        return change;
    }
    
    private BreakingChange.BreakingChangeType determineModificationChangeType(ElementInfo oldElement, ElementInfo newElement) {
        // Check signature changes first
        if (!Objects.equals(oldElement.getSignature(), newElement.getSignature())) {
            return BreakingChange.BreakingChangeType.CHANGED_METHOD_SIGNATURE;
        }
        
        // Check return type changes
        if (!Objects.equals(oldElement.getReturnType(), newElement.getReturnType())) {
            return BreakingChange.BreakingChangeType.CHANGED_RETURN_TYPE;
        }
        
        // Check visibility reduction
        if (isVisibilityReduced(oldElement, newElement)) {
            return BreakingChange.BreakingChangeType.VISIBILITY_REDUCED;
        }
        
        // Check modifier changes
        if (!Objects.equals(oldElement.getModifiers(), newElement.getModifiers())) {
            return BreakingChange.BreakingChangeType.MODIFIER_CHANGED;
        }
        
        // Default to annotation changed
        return BreakingChange.BreakingChangeType.ANNOTATION_CHANGED;
    }
    
    private ComparisonSummary createSummary(List<BreakingChange> breakingChanges,
                                          List<ElementInfo> addedElements,
                                          List<ElementInfo> removedElements,
                                          List<ElementModification> modifiedElements) {
        ComparisonSummary summary = new ComparisonSummary();
        
        summary.setTotalBreakingChanges(breakingChanges.size());
        summary.setAddedElements(addedElements.size());
        summary.setRemovedElements(removedElements.size());
        summary.setModifiedElements(modifiedElements.size());
        
        // Count by severity
        Map<BreakingChange.Severity, Long> severityCounts = breakingChanges.stream()
                .collect(Collectors.groupingBy(BreakingChange::getSeverity, Collectors.counting()));
        
        summary.setHighSeverityChanges(severityCounts.getOrDefault(BreakingChange.Severity.HIGH, 0L).intValue());
        summary.setMediumSeverityChanges(severityCounts.getOrDefault(BreakingChange.Severity.MEDIUM, 0L).intValue());
        summary.setLowSeverityChanges(severityCounts.getOrDefault(BreakingChange.Severity.LOW, 0L).intValue());
        
        // Determine backward compatibility
        summary.setBackwardCompatible(breakingChanges.isEmpty());
        
        return summary;
    }
}