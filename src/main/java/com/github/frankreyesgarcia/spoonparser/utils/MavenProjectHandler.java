package com.github.frankreyesgarcia.spoonparser.utils;

import com.github.frankreyesgarcia.spoonparser.analyzer.SpoonCodeAnalyzer;
import com.github.frankreyesgarcia.spoonparser.model.AnalysisResult;
import com.github.frankreyesgarcia.spoonparser.model.ModuleAnalysisResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Handles Maven project structure analysis, including multi-module projects.
 */
public class MavenProjectHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MavenProjectHandler.class);
    
    private final SpoonCodeAnalyzer analyzer;
    
    public MavenProjectHandler() {
        this.analyzer = new SpoonCodeAnalyzer();
    }
    
    public MavenProjectHandler(SpoonCodeAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
    
    /**
     * Analyze a Maven project, handling both single-module and multi-module projects.
     */
    public AnalysisResult analyzeProject(Path projectPath) {
        long startTime = System.currentTimeMillis();
        
        logger.info("Analyzing Maven project at: {}", projectPath);
        
        AnalysisResult result = new AnalysisResult(projectPath.toString());
        List<String> errors = new ArrayList<>();
        
        try {
            if (isMultiModuleProject(projectPath)) {
                analyzeMultiModuleProject(projectPath, result, errors);
            } else {
                analyzeSingleModuleProject(projectPath, result, errors);
            }
        } catch (Exception e) {
            logger.error("Error analyzing Maven project", e);
            errors.add("Project analysis error: " + e.getMessage());
        }
        
        result.setAnalysisTimeMs(System.currentTimeMillis() - startTime);
        result.setErrors(errors.isEmpty() ? null : errors);
        
        return result;
    }
    
    /**
     * Check if the project is a multi-module Maven project.
     */
    public boolean isMultiModuleProject(Path projectPath) {
        Path pomPath = projectPath.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return false;
        }
        
        try {
            Model model = readPomModel(pomPath);
            return model.getModules() != null && !model.getModules().isEmpty();
        } catch (Exception e) {
            logger.warn("Error reading POM file to check for modules: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get list of module paths in a multi-module project.
     */
    public List<Path> getModulePaths(Path projectPath) {
        List<Path> modulePaths = new ArrayList<>();
        
        Path pomPath = projectPath.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return modulePaths;
        }
        
        try {
            Model model = readPomModel(pomPath);
            if (model.getModules() != null) {
                for (String module : model.getModules()) {
                    Path modulePath = projectPath.resolve(module);
                    if (Files.exists(modulePath) && Files.isDirectory(modulePath)) {
                        modulePaths.add(modulePath);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error reading modules from POM: {}", e.getMessage());
        }
        
        return modulePaths;
    }
    
    /**
     * Find all Maven modules recursively, including nested multi-module projects.
     */
    public List<Path> findAllModules(Path projectPath) {
        List<Path> allModules = new ArrayList<>();
        Set<Path> visited = new HashSet<>();
        
        findModulesRecursively(projectPath, allModules, visited);
        
        return allModules;
    }
    
    private void findModulesRecursively(Path currentPath, List<Path> allModules, Set<Path> visited) {
        if (visited.contains(currentPath)) {
            return;
        }
        visited.add(currentPath);
        
        Path pomPath = currentPath.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return;
        }
        
        try {
            Model model = readPomModel(pomPath);
            
            // Check if this is a module with source code (has src directory or no modules)
            Path srcPath = currentPath.resolve("src");
            boolean hasSource = Files.exists(srcPath) && Files.isDirectory(srcPath);
            boolean hasModules = model.getModules() != null && !model.getModules().isEmpty();
            
            if (hasSource || !hasModules) {
                allModules.add(currentPath);
            }
            
            // Recursively check sub-modules
            if (hasModules) {
                for (String module : model.getModules()) {
                    Path modulePath = currentPath.resolve(module);
                    if (Files.exists(modulePath) && Files.isDirectory(modulePath)) {
                        findModulesRecursively(modulePath, allModules, visited);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Error processing module at {}: {}", currentPath, e.getMessage());
        }
    }
    
    private void analyzeMultiModuleProject(Path projectPath, AnalysisResult result, List<String> errors) {
        logger.info("Analyzing multi-module Maven project");
        
        List<Path> modules = findAllModules(projectPath);
        Map<String, ModuleAnalysisResult> moduleResults = new LinkedHashMap<>();
        List<com.github.frankreyesgarcia.spoonparser.model.ElementInfo> allElements = new ArrayList<>();
        
        for (Path modulePath : modules) {
            try {
                logger.info("Analyzing module: {}", modulePath);
                
                AnalysisResult moduleAnalysis = analyzer.analyzeProject(modulePath);
                
                String moduleName = getModuleName(modulePath);
                ModuleAnalysisResult moduleResult = new ModuleAnalysisResult(moduleName, modulePath.toString());
                
                if (moduleAnalysis.getElements() != null) {
                    moduleResult.setElements(moduleAnalysis.getElements());
                    allElements.addAll(moduleAnalysis.getElements());
                }
                
                if (moduleAnalysis.getErrors() != null) {
                    moduleResult.setCompilationErrors(moduleAnalysis.getErrors());
                }
                
                // Extract source files and dependencies
                moduleResult.setSourceFiles(findSourceFiles(modulePath));
                moduleResult.setDependencies(extractDependencies(modulePath));
                
                moduleResults.put(moduleName, moduleResult);
                
            } catch (Exception e) {
                logger.error("Error analyzing module {}: {}", modulePath, e.getMessage());
                errors.add("Module analysis error for " + modulePath + ": " + e.getMessage());
            }
        }
        
        result.setModuleResults(moduleResults);
        result.setElements(allElements);
        
        logger.info("Multi-module analysis completed. Analyzed {} modules with {} total elements", 
                   modules.size(), allElements.size());
    }
    
    private void analyzeSingleModuleProject(Path projectPath, AnalysisResult result, List<String> errors) {
        logger.info("Analyzing single-module Maven project");
        
        AnalysisResult moduleAnalysis = analyzer.analyzeProject(projectPath);
        
        // Copy results from module analysis
        result.setElements(moduleAnalysis.getElements());
        if (moduleAnalysis.getErrors() != null) {
            errors.addAll(moduleAnalysis.getErrors());
        }
        
        // Create a single module result for consistency
        String moduleName = getModuleName(projectPath);
        ModuleAnalysisResult moduleResult = new ModuleAnalysisResult(moduleName, projectPath.toString());
        moduleResult.setElements(moduleAnalysis.getElements());
        moduleResult.setSourceFiles(findSourceFiles(projectPath));
        moduleResult.setDependencies(extractDependencies(projectPath));
        
        Map<String, ModuleAnalysisResult> moduleResults = new HashMap<>();
        moduleResults.put(moduleName, moduleResult);
        result.setModuleResults(moduleResults);
    }
    
    private String getModuleName(Path modulePath) {
        Path pomPath = modulePath.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            try {
                Model model = readPomModel(pomPath);
                if (model.getArtifactId() != null) {
                    return model.getArtifactId();
                }
            } catch (Exception e) {
                logger.warn("Error reading module name from POM: {}", e.getMessage());
            }
        }
        
        return modulePath.getFileName().toString();
    }
    
    private List<String> findSourceFiles(Path modulePath) {
        List<String> sourceFiles = new ArrayList<>();
        
        // Standard Maven directories
        Path[] sourceDirs = {
            modulePath.resolve("src/main/java"),
            modulePath.resolve("src/test/java")
        };
        
        for (Path sourceDir : sourceDirs) {
            if (Files.exists(sourceDir)) {
                try (Stream<Path> files = Files.walk(sourceDir)) {
                    files.filter(Files::isRegularFile)
                         .filter(path -> path.toString().endsWith(".java"))
                         .map(Path::toString)
                         .forEach(sourceFiles::add);
                } catch (IOException e) {
                    logger.warn("Error scanning source directory {}: {}", sourceDir, e.getMessage());
                }
            }
        }
        
        return sourceFiles;
    }
    
    private List<String> extractDependencies(Path modulePath) {
        List<String> dependencies = new ArrayList<>();
        
        Path pomPath = modulePath.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            try {
                Model model = readPomModel(pomPath);
                if (model.getDependencies() != null) {
                    model.getDependencies().forEach(dep -> {
                        String depString = String.format("%s:%s:%s",
                            dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
                        dependencies.add(depString);
                    });
                }
            } catch (Exception e) {
                logger.warn("Error reading dependencies from POM: {}", e.getMessage());
            }
        }
        
        return dependencies;
    }
    
    private Model readPomModel(Path pomPath) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            return reader.read(fileReader);
        }
    }
    
    /**
     * Get project information from POM file.
     */
    public ProjectInfo getProjectInfo(Path projectPath) {
        Path pomPath = projectPath.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return new ProjectInfo(projectPath.getFileName().toString(), "unknown", "unknown");
        }
        
        try {
            Model model = readPomModel(pomPath);
            return new ProjectInfo(
                model.getArtifactId() != null ? model.getArtifactId() : "unknown",
                model.getGroupId() != null ? model.getGroupId() : "unknown",
                model.getVersion() != null ? model.getVersion() : "unknown"
            );
        } catch (Exception e) {
            logger.warn("Error reading project info from POM: {}", e.getMessage());
            return new ProjectInfo(projectPath.getFileName().toString(), "unknown", "unknown");
        }
    }
    
    /**
     * Simple holder for project information.
     */
    public static class ProjectInfo {
        private final String artifactId;
        private final String groupId;
        private final String version;
        
        public ProjectInfo(String artifactId, String groupId, String version) {
            this.artifactId = artifactId;
            this.groupId = groupId;
            this.version = version;
        }
        
        public String getArtifactId() { return artifactId; }
        public String getGroupId() { return groupId; }
        public String getVersion() { return version; }
        
        @Override
        public String toString() {
            return String.format("%s:%s:%s", groupId, artifactId, version);
        }
    }
}