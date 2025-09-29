package com.github.frankreyesgarcia.spoonparser.cli;

import com.github.frankreyesgarcia.spoonparser.analyzer.SpoonCodeAnalyzer;
import com.github.frankreyesgarcia.spoonparser.comparator.VersionComparator;
import com.github.frankreyesgarcia.spoonparser.model.AnalysisResult;
import com.github.frankreyesgarcia.spoonparser.model.ComparisonResult;
import com.github.frankreyesgarcia.spoonparser.model.ElementInfo;
import com.github.frankreyesgarcia.spoonparser.utils.JsonOutputGenerator;
import com.github.frankreyesgarcia.spoonparser.utils.MavenProjectHandler;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command-line interface for the Spoon Parser Maven Code Analyzer.
 */
public class SpoonParserCLI {
    
    private static final Logger logger = LoggerFactory.getLogger(SpoonParserCLI.class);
    
    private final JsonOutputGenerator jsonGenerator;
    private final MavenProjectHandler projectHandler;
    private final VersionComparator comparator;
    
    public SpoonParserCLI() {
        this.jsonGenerator = new JsonOutputGenerator();
        this.projectHandler = new MavenProjectHandler();
        this.comparator = new VersionComparator();
    }
    
    public static void main(String[] args) {
        SpoonParserCLI cli = new SpoonParserCLI();
        cli.run(args);
    }
    
    public void run(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        
        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("help")) {
                printHelp(formatter, options);
                return;
            }
            
            if (cmd.hasOption("analyze")) {
                handleAnalyzeCommand(cmd);
            } else if (cmd.hasOption("compare")) {
                handleCompareCommand(cmd);
            } else if (cmd.hasOption("extract-lines")) {
                handleExtractLinesCommand(cmd);
            } else {
                System.err.println("No command specified. Use --help for usage information.");
                System.exit(1);
            }
            
        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            printHelp(formatter, options);
            System.exit(1);
        } catch (Exception e) {
            logger.error("Error executing command", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private Options createOptions() {
        Options options = new Options();
        
        // Commands
        options.addOption("a", "analyze", true, "Analyze a Maven project directory");
        options.addOption("c", "compare", true, "Compare two analysis results (format: old.json,new.json)");
        options.addOption("e", "extract-lines", true, "Extract elements from specific lines (format: file:line1,line2,...)");
        
        // Options
        options.addOption("o", "output", true, "Output file path (default: stdout)");
        options.addOption("f", "format", true, "Output format: json (default), compact-json");
        options.addOption("p", "project", true, "Project path for line extraction");
        options.addOption("v", "verbose", false, "Enable verbose logging");
        options.addOption("h", "help", false, "Show help message");
        
        // Version labels for comparison
        options.addOption(null, "old-version", true, "Label for old version in comparison");
        options.addOption(null, "new-version", true, "Label for new version in comparison");
        
        return options;
    }
    
    private void printHelp(HelpFormatter formatter, Options options) {
        System.out.println("Spoon Parser - Maven Code Analyzer");
        System.out.println("Extract element details and detect breaking changes in Maven projects");
        System.out.println();
        
        formatter.printHelp("spoonparser", options);
        
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Analyze a Maven project");
        System.out.println("  spoonparser --analyze /path/to/maven/project");
        System.out.println();
        System.out.println("  # Compare two versions");
        System.out.println("  spoonparser --compare old-analysis.json,new-analysis.json");
        System.out.println();
        System.out.println("  # Extract elements from specific lines");
        System.out.println("  spoonparser --extract-lines src/main/java/Example.java:10,15,20 --project /path/to/project");
        System.out.println();
        System.out.println("  # Save output to file");
        System.out.println("  spoonparser --analyze /path/to/project --output analysis.json");
    }
    
    private void handleAnalyzeCommand(CommandLine cmd) throws Exception {
        String projectPath = cmd.getOptionValue("analyze");
        Path path = Paths.get(projectPath);
        
        if (!path.toFile().exists()) {
            throw new IllegalArgumentException("Project path does not exist: " + projectPath);
        }
        
        logger.info("Analyzing project: {}", projectPath);
        
        AnalysisResult result = projectHandler.analyzeProject(path);
        
        String output = formatOutput(result, cmd);
        writeOutput(output, cmd);
        
        // Print summary to stderr so it doesn't interfere with JSON output
        System.err.println("Analysis completed successfully");
        System.err.println("Total elements: " + result.getTotalElements());
        System.err.println("Analysis time: " + result.getAnalysisTimeMs() + "ms");
        
        if (result.getModuleResults() != null) {
            System.err.println("Modules analyzed: " + result.getModuleResults().size());
        }
    }
    
    private void handleCompareCommand(CommandLine cmd) throws Exception {
        String compareValue = cmd.getOptionValue("compare");
        String[] parts = compareValue.split(",");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("Compare option requires two files: old.json,new.json");
        }
        
        File oldFile = new File(parts[0].trim());
        File newFile = new File(parts[1].trim());
        
        if (!oldFile.exists()) {
            throw new IllegalArgumentException("Old version file does not exist: " + oldFile.getPath());
        }
        if (!newFile.exists()) {
            throw new IllegalArgumentException("New version file does not exist: " + newFile.getPath());
        }
        
        logger.info("Comparing versions: {} -> {}", oldFile.getName(), newFile.getName());
        
        AnalysisResult oldResult = jsonGenerator.readAnalysisResultFromFile(oldFile);
        AnalysisResult newResult = jsonGenerator.readAnalysisResultFromFile(newFile);
        
        String oldVersionLabel = cmd.getOptionValue("old-version", oldFile.getName());
        String newVersionLabel = cmd.getOptionValue("new-version", newFile.getName());
        
        ComparisonResult result = comparator.compare(oldResult, newResult, oldVersionLabel, newVersionLabel);
        
        String output = formatOutput(result, cmd);
        writeOutput(output, cmd);
        
        // Print summary to stderr
        System.err.println("Comparison completed successfully");
        if (result.getSummary() != null) {
            System.err.println("Breaking changes: " + result.getSummary().getTotalBreakingChanges());
            System.err.println("Added elements: " + result.getSummary().getAddedElements());
            System.err.println("Removed elements: " + result.getSummary().getRemovedElements());
            System.err.println("Modified elements: " + result.getSummary().getModifiedElements());
            System.err.println("Backward compatible: " + result.getSummary().isBackwardCompatible());
        }
    }
    
    private void handleExtractLinesCommand(CommandLine cmd) throws Exception {
        String extractValue = cmd.getOptionValue("extract-lines");
        String projectPath = cmd.getOptionValue("project");
        
        if (projectPath == null) {
            throw new IllegalArgumentException("Project path is required for line extraction (--project)");
        }
        
        // Parse format: file:line1,line2,line3
        String[] parts = extractValue.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid extract-lines format. Use: file:line1,line2,line3");
        }
        
        String fileName = parts[0];
        String[] lineStrings = parts[1].split(",");
        Set<Integer> lineNumbers = Arrays.stream(lineStrings)
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        
        Path projectPathObj = Paths.get(projectPath);
        Path sourceFile = projectPathObj.resolve(fileName);
        
        if (!sourceFile.toFile().exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFile);
        }
        
        logger.info("Extracting elements from {} at lines: {}", fileName, lineNumbers);
        
        // First analyze the project to build the model
        SpoonCodeAnalyzer analyzer = new SpoonCodeAnalyzer();
        analyzer.analyzeProject(projectPathObj);
        
        // Extract elements from specific lines
        List<ElementInfo> elements = analyzer.extractElementsFromLines(sourceFile, lineNumbers);
        
        // Create a simple result structure
        AnalysisResult result = new AnalysisResult(projectPath);
        result.setElements(elements);
        result.setAnalysisTimeMs(0); // Not measuring time for line extraction
        
        String output = formatOutput(result, cmd);
        writeOutput(output, cmd);
        
        // Print summary to stderr
        System.err.println("Line extraction completed");
        System.err.println("Elements found: " + elements.size());
    }
    
    private String formatOutput(Object result, CommandLine cmd) throws Exception {
        String format = cmd.getOptionValue("format", "json");
        
        switch (format.toLowerCase()) {
            case "json":
                if (result instanceof AnalysisResult) {
                    return jsonGenerator.toJson((AnalysisResult) result);
                } else if (result instanceof ComparisonResult) {
                    return jsonGenerator.toJson((ComparisonResult) result);
                } else {
                    throw new IllegalArgumentException("Unsupported result type: " + result.getClass());
                }
            case "compact-json":
                return jsonGenerator.toCompactJson(result);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
    
    private void writeOutput(String output, CommandLine cmd) throws Exception {
        String outputPath = cmd.getOptionValue("output");
        
        if (outputPath != null) {
            File outputFile = new File(outputPath);
            java.nio.file.Files.write(outputFile.toPath(), output.getBytes());
            System.err.println("Output written to: " + outputFile.getAbsolutePath());
        } else {
            System.out.println(output);
        }
    }
}