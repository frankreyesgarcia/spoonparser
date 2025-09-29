package com.github.frankreyesgarcia.spoonparser;

import com.github.frankreyesgarcia.spoonparser.comparator.VersionComparator;
import com.github.frankreyesgarcia.spoonparser.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for VersionComparator.
 */
public class VersionComparatorTest {
    
    private VersionComparator comparator;
    
    @BeforeEach
    void setUp() {
        comparator = new VersionComparator();
    }
    
    @Test
    void testCompareIdenticalVersions() {
        AnalysisResult v1 = createSampleAnalysisResult();
        AnalysisResult v2 = createSampleAnalysisResult();
        
        ComparisonResult result = comparator.compare(v1, v2);
        
        assertNotNull(result);
        assertNotNull(result.getSummary());
        assertTrue(result.getSummary().isBackwardCompatible());
        assertEquals(0, result.getSummary().getTotalBreakingChanges());
        assertEquals(0, result.getSummary().getAddedElements());
        assertEquals(0, result.getSummary().getRemovedElements());
    }
    
    @Test
    void testCompareWithAddedElement() {
        AnalysisResult v1 = createSampleAnalysisResult();
        AnalysisResult v2 = createSampleAnalysisResult();
        
        // Add an element to v2
        ElementInfo newElement = new ElementInfo();
        newElement.setFullyQualifiedName("com.example.NewClass");
        newElement.setType(ElementType.CLASS);
        newElement.setModifiers(new HashSet<>(Arrays.asList("PUBLIC")));
        
        // Create a new mutable list for v2
        java.util.List<ElementInfo> v2Elements = new java.util.ArrayList<>(v2.getElements());
        v2Elements.add(newElement);
        v2.setElements(v2Elements);
        
        ComparisonResult result = comparator.compare(v1, v2);
        
        assertNotNull(result);
        assertEquals(1, result.getSummary().getAddedElements());
        assertEquals(0, result.getSummary().getRemovedElements());
        assertTrue(result.getSummary().isBackwardCompatible()); // Adding elements is not breaking
    }
    
    @Test
    void testCompareWithRemovedPublicElement() {
        AnalysisResult v1 = createSampleAnalysisResult();
        AnalysisResult v2 = new AnalysisResult();
        v2.setElements(Arrays.asList()); // Empty - all elements removed
        
        ComparisonResult result = comparator.compare(v1, v2);
        
        assertNotNull(result);
        assertEquals(1, result.getSummary().getRemovedElements());
        assertEquals(0, result.getSummary().getAddedElements());
        assertFalse(result.getSummary().isBackwardCompatible()); // Removing public elements is breaking
        assertTrue(result.getSummary().getTotalBreakingChanges() > 0);
    }
    
    private AnalysisResult createSampleAnalysisResult() {
        AnalysisResult result = new AnalysisResult();
        
        ElementInfo element = new ElementInfo();
        element.setFullyQualifiedName("com.example.TestClass");
        element.setType(ElementType.CLASS);
        element.setModifiers(new HashSet<>(Arrays.asList("PUBLIC")));
        
        result.setElements(new java.util.ArrayList<>(Arrays.asList(element)));
        return result;
    }
}