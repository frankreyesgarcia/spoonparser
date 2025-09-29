package com.github.frankreyesgarcia.spoonparser;

import com.github.frankreyesgarcia.spoonparser.analyzer.SpoonCodeAnalyzer;
import com.github.frankreyesgarcia.spoonparser.model.AnalysisResult;
import com.github.frankreyesgarcia.spoonparser.model.ElementInfo;
import com.github.frankreyesgarcia.spoonparser.model.ElementType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SpoonCodeAnalyzer.
 */
public class SpoonCodeAnalyzerTest {
    
    private SpoonCodeAnalyzer analyzer;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        analyzer = new SpoonCodeAnalyzer();
    }
    
    @Test
    void testAnalyzeSimpleProject() throws IOException {
        // Create a simple Maven project structure
        Path srcDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcDir);
        
        // Create a simple Java class
        String javaContent = 
            "package com.example;\n" +
            "\n" +
            "public class TestClass {\n" +
            "    private String name;\n" +
            "    \n" +
            "    public TestClass(String name) {\n" +
            "        this.name = name;\n" +
            "    }\n" +
            "    \n" +
            "    public String getName() {\n" +
            "        return name;\n" +
            "    }\n" +
            "}\n";
            
        Files.write(srcDir.resolve("TestClass.java"), javaContent.getBytes());
        
        // Analyze the project
        AnalysisResult result = analyzer.analyzeProject(tempDir);
        
        assertNotNull(result);
        assertNotNull(result.getElements());
        assertFalse(result.getElements().isEmpty());
        
        // Check that we found the class
        boolean foundClass = result.getElements().stream()
            .anyMatch(e -> e.getType() == ElementType.CLASS && 
                          "com.example.TestClass".equals(e.getFullyQualifiedName()));
        assertTrue(foundClass, "Should find TestClass");
        
        // Check that we found the method
        boolean foundMethod = result.getElements().stream()
            .anyMatch(e -> e.getType() == ElementType.METHOD && 
                          e.getFullyQualifiedName().contains("getName"));
        assertTrue(foundMethod, "Should find getName method");
        
        // Check that we found the field
        boolean foundField = result.getElements().stream()
            .anyMatch(e -> e.getType() == ElementType.FIELD && 
                          e.getFullyQualifiedName().contains("name"));
        assertTrue(foundField, "Should find name field");
    }
    
    @Test
    void testAnalyzeEmptyProject() {
        AnalysisResult result = analyzer.analyzeProject(tempDir);
        
        assertNotNull(result);
        // Should return empty or minimal results for empty project
        assertTrue(result.getElements() == null || result.getElements().isEmpty());
    }
}