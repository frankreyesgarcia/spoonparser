package com.github.frankreyesgarcia.spoonparser.analyzer;

import com.github.frankreyesgarcia.spoonparser.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core analyzer that uses Spoon framework to parse Java code and extract element information.
 */
public class SpoonCodeAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(SpoonCodeAnalyzer.class);
    
    private final Launcher launcher;
    private CtModel model;
    
    public SpoonCodeAnalyzer() {
        this.launcher = new Launcher();
        setupLauncher();
    }
    
    private void setupLauncher() {
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setLevel("ERROR");
    }
    
    /**
     * Analyze a Maven project and extract element information.
     */
    public AnalysisResult analyzeProject(Path projectPath) {
        long startTime = System.currentTimeMillis();
        AnalysisResult result = new AnalysisResult(projectPath.toString());
        List<String> errors = new ArrayList<>();
        
        try {
            logger.info("Starting analysis of project: {}", projectPath);
            
            // Find source directories
            List<Path> sourcePaths = findSourceDirectories(projectPath);
            if (sourcePaths.isEmpty()) {
                errors.add("No source directories found in project");
                result.setErrors(errors);
                return result;
            }
            
            // Add source paths to launcher
            for (Path sourcePath : sourcePaths) {
                launcher.addInputResource(sourcePath.toString());
            }
            
            // Build the model
            model = launcher.buildModel();
            
            // Extract elements
            List<ElementInfo> elements = extractAllElements();
            result.setElements(elements);
            
            logger.info("Analysis completed. Found {} elements", elements.size());
            
        } catch (Exception e) {
            logger.error("Error during analysis", e);
            errors.add("Analysis error: " + e.getMessage());
        }
        
        result.setAnalysisTimeMs(System.currentTimeMillis() - startTime);
        result.setErrors(errors.isEmpty() ? null : errors);
        return result;
    }
    
    /**
     * Extract elements from specific line numbers in a source file.
     */
    public List<ElementInfo> extractElementsFromLines(Path sourceFile, Set<Integer> lineNumbers) {
        if (model == null) {
            throw new IllegalStateException("Model not built. Call analyzeProject first.");
        }
        
        List<ElementInfo> elements = new ArrayList<>();
        String absolutePath = sourceFile.toAbsolutePath().toString();
        
        // Get all elements from the model
        model.getAllTypes().forEach(type -> {
            if (isElementAtLines(type, absolutePath, lineNumbers)) {
                elements.add(extractElementInfo(type));
            }
            
            // Check methods
            type.getMethods().forEach(method -> {
                if (isElementAtLines(method, absolutePath, lineNumbers)) {
                    elements.add(extractElementInfo(method));
                }
            });
            
            // Check fields
            type.getFields().forEach(field -> {
                if (isElementAtLines(field, absolutePath, lineNumbers)) {
                    elements.add(extractElementInfo(field));
                }
            });
            
            // Check constructors
            if (type instanceof CtClass) {
                ((CtClass<?>) type).getConstructors().forEach(constructor -> {
                    if (isElementAtLines(constructor, absolutePath, lineNumbers)) {
                        elements.add(extractElementInfo(constructor));
                    }
                });
            }
        });
        
        return elements;
    }
    
    private boolean isElementAtLines(CtElement element, String filePath, Set<Integer> lineNumbers) {
        SourcePosition position = element.getPosition();
        if (position == null || !position.isValidPosition()) {
            return false;
        }
        
        String elementFilePath = position.getFile().getAbsolutePath();
        if (!elementFilePath.equals(filePath)) {
            return false;
        }
        
        int startLine = position.getLine();
        int endLine = position.getEndLine();
        
        return lineNumbers.stream().anyMatch(line -> line >= startLine && line <= endLine);
    }
    
    private List<Path> findSourceDirectories(Path projectPath) {
        List<Path> sourcePaths = new ArrayList<>();
        
        // Standard Maven source directories
        Path mainJava = projectPath.resolve("src/main/java");
        if (mainJava.toFile().exists() && mainJava.toFile().isDirectory()) {
            sourcePaths.add(mainJava);
        }
        
        Path testJava = projectPath.resolve("src/test/java");
        if (testJava.toFile().exists() && testJava.toFile().isDirectory()) {
            sourcePaths.add(testJava);
        }
        
        // Look for multi-module structure
        File[] subdirs = projectPath.toFile().listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                if (!subdir.getName().startsWith(".") && !subdir.getName().equals("target")) {
                    Path subMainJava = subdir.toPath().resolve("src/main/java");
                    if (subMainJava.toFile().exists()) {
                        sourcePaths.add(subMainJava);
                    }
                }
            }
        }
        
        return sourcePaths;
    }
    
    private List<ElementInfo> extractAllElements() {
        List<ElementInfo> elements = new ArrayList<>();
        
        // Extract all types (classes, interfaces, enums, annotations)
        model.getAllTypes().forEach(type -> {
            elements.add(extractElementInfo(type));
            
            // Extract methods
            type.getMethods().forEach(method -> elements.add(extractElementInfo(method)));
            
            // Extract fields
            type.getFields().forEach(field -> elements.add(extractElementInfo(field)));
            
            // Extract constructors
            if (type instanceof CtClass) {
                ((CtClass<?>) type).getConstructors().forEach(constructor -> elements.add(extractElementInfo(constructor)));
            }
        });
        
        return elements;
    }
    
    private ElementInfo extractElementInfo(CtElement element) {
        ElementInfo info = new ElementInfo();
        
        if (element instanceof CtType) {
            extractTypeInfo((CtType<?>) element, info);
        } else if (element instanceof CtMethod) {
            extractMethodInfo((CtMethod<?>) element, info);
        } else if (element instanceof CtField) {
            extractFieldInfo((CtField<?>) element, info);
        } else if (element instanceof CtConstructor) {
            extractConstructorInfo((CtConstructor<?>) element, info);
        }
        
        // Extract common information
        extractCommonInfo(element, info);
        
        return info;
    }
    
    private void extractTypeInfo(CtType<?> type, ElementInfo info) {
        info.setFullyQualifiedName(type.getQualifiedName());
        
        if (type instanceof CtClass) {
            info.setType(ElementType.CLASS);
            CtClass<?> clazz = (CtClass<?>) type;
            if (clazz.getSuperclass() != null) {
                info.setSuperClass(clazz.getSuperclass().getQualifiedName());
            }
        } else if (type instanceof CtInterface) {
            info.setType(ElementType.INTERFACE);
        } else if (type instanceof CtEnum) {
            info.setType(ElementType.ENUM);
        } else if (type instanceof CtAnnotationType) {
            info.setType(ElementType.ANNOTATION);
        }
        
        // Extract interfaces
        Set<CtTypeReference<?>> superInterfaces = type.getSuperInterfaces();
        if (!superInterfaces.isEmpty()) {
            List<String> interfaces = superInterfaces.stream()
                    .map(CtTypeReference::getQualifiedName)
                    .collect(Collectors.toList());
            info.setInterfaces(interfaces);
        }
        
        // Extract modifiers
        Set<String> modifiers = type.getModifiers().stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
        info.setModifiers(modifiers);
    }
    
    private void extractMethodInfo(CtMethod<?> method, ElementInfo info) {
        String className = method.getDeclaringType().getQualifiedName();
        String methodName = method.getSimpleName();
        String signature = generateMethodSignature(method);
        
        info.setFullyQualifiedName(className + "#" + methodName);
        info.setType(ElementType.METHOD);
        info.setSignature(signature);
        
        // Return type
        if (method.getType() != null) {
            info.setReturnType(method.getType().getQualifiedName());
        }
        
        // Parameters
        List<ParameterInfo> parameters = method.getParameters().stream()
                .map(this::extractParameterInfo)
                .collect(Collectors.toList());
        info.setParameters(parameters);
        
        // Modifiers
        Set<String> modifiers = method.getModifiers().stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
        info.setModifiers(modifiers);
    }
    
    private void extractFieldInfo(CtField<?> field, ElementInfo info) {
        String className = field.getDeclaringType().getQualifiedName();
        String fieldName = field.getSimpleName();
        
        info.setFullyQualifiedName(className + "#" + fieldName);
        info.setType(ElementType.FIELD);
        
        if (field.getType() != null) {
            info.setFieldType(field.getType().getQualifiedName());
        }
        
        // Modifiers
        Set<String> modifiers = field.getModifiers().stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
        info.setModifiers(modifiers);
    }
    
    private void extractConstructorInfo(CtConstructor<?> constructor, ElementInfo info) {
        String className = constructor.getDeclaringType().getQualifiedName();
        String signature = generateConstructorSignature(constructor);
        
        info.setFullyQualifiedName(className + "#<init>");
        info.setType(ElementType.CONSTRUCTOR);
        info.setSignature(signature);
        
        // Parameters
        List<ParameterInfo> parameters = constructor.getParameters().stream()
                .map(this::extractParameterInfo)
                .collect(Collectors.toList());
        info.setParameters(parameters);
        
        // Modifiers
        Set<String> modifiers = constructor.getModifiers().stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
        info.setModifiers(modifiers);
    }
    
    private void extractCommonInfo(CtElement element, ElementInfo info) {
        // Source position information
        SourcePosition position = element.getPosition();
        if (position != null && position.isValidPosition()) {
            info.setLineNumber(position.getLine());
            if (position.getFile() != null) {
                info.setSourceFile(position.getFile().getPath());
            }
        }
        
        // Annotations - check for annotated elements
        if (element instanceof CtMethod || element instanceof CtField || element instanceof CtType || element instanceof CtConstructor) {
            List<CtAnnotation<? extends Annotation>> annotations = null;
            
            if (element instanceof CtMethod) {
                annotations = ((CtMethod<?>) element).getAnnotations();
            } else if (element instanceof CtField) {
                annotations = ((CtField<?>) element).getAnnotations();
            } else if (element instanceof CtType) {
                annotations = ((CtType<?>) element).getAnnotations();
            } else if (element instanceof CtConstructor) {
                annotations = ((CtConstructor<?>) element).getAnnotations();
            }
            
            if (annotations != null && !annotations.isEmpty()) {
                List<AnnotationInfo> annotationInfos = annotations.stream()
                        .map(this::extractAnnotationInfo)
                        .collect(Collectors.toList());
                info.setAnnotations(annotationInfos);
            }
        }
    }
    
    private ParameterInfo extractParameterInfo(CtParameter<?> parameter) {
        ParameterInfo paramInfo = new ParameterInfo();
        paramInfo.setName(parameter.getSimpleName());
        
        if (parameter.getType() != null) {
            paramInfo.setType(parameter.getType().getSimpleName());
            paramInfo.setFullyQualifiedType(parameter.getType().getQualifiedName());
        }
        
        paramInfo.setFinal(parameter.getModifiers().contains(ModifierKind.FINAL));
        paramInfo.setVarArgs(parameter.isVarArgs());
        
        // Annotations
        List<AnnotationInfo> annotations = parameter.getAnnotations().stream()
                .map(this::extractAnnotationInfo)
                .collect(Collectors.toList());
        if (!annotations.isEmpty()) {
            paramInfo.setAnnotations(annotations);
        }
        
        return paramInfo;
    }
    
    private AnnotationInfo extractAnnotationInfo(CtAnnotation<?> annotation) {
        AnnotationInfo annInfo = new AnnotationInfo();
        
        if (annotation.getAnnotationType() != null) {
            annInfo.setName(annotation.getAnnotationType().getSimpleName());
            annInfo.setFullyQualifiedName(annotation.getAnnotationType().getQualifiedName());
        }
        
        // Extract annotation values
        Map<String, Object> values = new HashMap<>();
        annotation.getValues().forEach((key, value) -> {
            values.put(key, value != null ? value.toString() : null);
        });
        
        if (!values.isEmpty()) {
            annInfo.setValues(values);
        }
        
        return annInfo;
    }
    
    private String generateMethodSignature(CtMethod<?> method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getSimpleName()).append("(");
        
        List<String> paramTypes = method.getParameters().stream()
                .map(p -> p.getType() != null ? p.getType().getQualifiedName() : "unknown")
                .collect(Collectors.toList());
        
        sb.append(String.join(", ", paramTypes));
        sb.append(")");
        
        if (method.getType() != null) {
            sb.append(" : ").append(method.getType().getQualifiedName());
        }
        
        return sb.toString();
    }
    
    private String generateConstructorSignature(CtConstructor<?> constructor) {
        StringBuilder sb = new StringBuilder();
        sb.append("<init>(");
        
        List<String> paramTypes = constructor.getParameters().stream()
                .map(p -> p.getType() != null ? p.getType().getQualifiedName() : "unknown")
                .collect(Collectors.toList());
        
        sb.append(String.join(", ", paramTypes));
        sb.append(")");
        
        return sb.toString();
    }
    
    public CtModel getModel() {
        return model;
    }
}