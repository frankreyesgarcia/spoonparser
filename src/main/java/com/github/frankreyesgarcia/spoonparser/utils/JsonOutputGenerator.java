package com.github.frankreyesgarcia.spoonparser.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.frankreyesgarcia.spoonparser.model.AnalysisResult;
import com.github.frankreyesgarcia.spoonparser.model.ComparisonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Utility class for generating structured JSON output from analysis results.
 */
public class JsonOutputGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonOutputGenerator.class);
    
    private final ObjectMapper objectMapper;
    
    public JsonOutputGenerator() {
        this.objectMapper = createObjectMapper();
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure for readable output
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Support for Java 8 time types
        mapper.registerModule(new JavaTimeModule());
        
        return mapper;
    }
    
    /**
     * Convert analysis result to JSON string.
     */
    public String toJson(AnalysisResult result) throws IOException {
        return objectMapper.writeValueAsString(result);
    }
    
    /**
     * Convert comparison result to JSON string.
     */
    public String toJson(ComparisonResult result) throws IOException {
        return objectMapper.writeValueAsString(result);
    }
    
    /**
     * Write analysis result to file.
     */
    public void writeToFile(AnalysisResult result, File outputFile) throws IOException {
        logger.info("Writing analysis result to file: {}", outputFile.getAbsolutePath());
        objectMapper.writeValue(outputFile, result);
    }
    
    /**
     * Write comparison result to file.
     */
    public void writeToFile(ComparisonResult result, File outputFile) throws IOException {
        logger.info("Writing comparison result to file: {}", outputFile.getAbsolutePath());
        objectMapper.writeValue(outputFile, result);
    }
    
    /**
     * Write analysis result to output stream.
     */
    public void writeToOutputStream(AnalysisResult result, OutputStream outputStream) throws IOException {
        objectMapper.writeValue(outputStream, result);
    }
    
    /**
     * Write comparison result to output stream.
     */
    public void writeToOutputStream(ComparisonResult result, OutputStream outputStream) throws IOException {
        objectMapper.writeValue(outputStream, result);
    }
    
    /**
     * Write analysis result to writer.
     */
    public void writeToWriter(AnalysisResult result, Writer writer) throws IOException {
        objectMapper.writeValue(writer, result);
    }
    
    /**
     * Write comparison result to writer.
     */
    public void writeToWriter(ComparisonResult result, Writer writer) throws IOException {
        objectMapper.writeValue(writer, result);
    }
    
    /**
     * Read analysis result from JSON string.
     */
    public AnalysisResult readAnalysisResultFromJson(String json) throws IOException {
        return objectMapper.readValue(json, AnalysisResult.class);
    }
    
    /**
     * Read comparison result from JSON string.
     */
    public ComparisonResult readComparisonResultFromJson(String json) throws IOException {
        return objectMapper.readValue(json, ComparisonResult.class);
    }
    
    /**
     * Read analysis result from file.
     */
    public AnalysisResult readAnalysisResultFromFile(File inputFile) throws IOException {
        logger.info("Reading analysis result from file: {}", inputFile.getAbsolutePath());
        return objectMapper.readValue(inputFile, AnalysisResult.class);
    }
    
    /**
     * Read comparison result from file.
     */
    public ComparisonResult readComparisonResultFromFile(File inputFile) throws IOException {
        logger.info("Reading comparison result from file: {}", inputFile.getAbsolutePath());
        return objectMapper.readValue(inputFile, ComparisonResult.class);
    }
    
    /**
     * Create a compact JSON output (single line, no indentation).
     */
    public String toCompactJson(Object result) throws IOException {
        ObjectMapper compactMapper = objectMapper.copy();
        compactMapper.disable(SerializationFeature.INDENT_OUTPUT);
        return compactMapper.writeValueAsString(result);
    }
    
    /**
     * Create a pretty-printed JSON output with custom settings.
     */
    public String toPrettyJson(Object result, boolean includeNulls) throws IOException {
        ObjectMapper prettyMapper = objectMapper.copy();
        
        if (!includeNulls) {
            prettyMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        }
        
        return prettyMapper.writeValueAsString(result);
    }
    
    /**
     * Get the configured ObjectMapper for advanced usage.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}