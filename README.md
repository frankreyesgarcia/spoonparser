# Spoon Parser - Maven Code Analyzer

A comprehensive Maven Code Analyzer that extracts element details from specific line numbers to detect breaking changes. Parse Maven projects, extract FQN, type, modifiers, parameters, return types, and annotations. Compare elements between versions by fully qualified name. Output structured JSON with method signatures, dependencies, and compatibility changes. Support multi-module projects and AST analysis using the Spoon framework.

## Features

### 🔍 **Comprehensive AST Analysis**
- **Full Java Code Parsing**: Leverages the Spoon framework for precise Abstract Syntax Tree analysis
- **Element Extraction**: Classes, interfaces, enums, annotations, methods, constructors, and fields
- **Rich Metadata**: Fully qualified names, modifiers, parameters, return types, annotations, and line numbers
- **Source File Mapping**: Tracks exact file locations and line numbers for all elements

### 📍 **Line-based Analysis**
- **Targeted Extraction**: Extract elements from specific line numbers in source files
- **Precise Targeting**: Identify classes, methods, or fields at exact source code locations
- **Development Integration**: Perfect for IDE plugins and code review tools

### 🏗️ **Multi-module Maven Support**
- **Complex Project Structures**: Handles nested and multi-module Maven projects
- **Recursive Discovery**: Automatically finds all modules in a project hierarchy
- **Consolidated Analysis**: Aggregates results across all modules with individual module reporting

### 🔄 **Breaking Change Detection** 
- **Version Comparison**: Compare two analysis results to identify API changes
- **Compatibility Analysis**: Detect breaking changes that affect backward compatibility
- **Severity Classification**: Categorize changes by impact level (LOW, MEDIUM, HIGH, CRITICAL)
- **Detailed Reporting**: Full change descriptions with before/after comparisons

### 📊 **Structured JSON Output**
- **Machine Readable**: Complete analysis results in well-structured JSON format
- **API Integration**: Easy to integrate with CI/CD pipelines and development tools
- **Comprehensive Data**: Includes method signatures, dependencies, and compatibility reports

## Installation

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Build from Source
```bash
git clone https://github.com/frankreyesgarcia/spoonparser.git
cd spoonparser
mvn clean package
```

This creates an executable JAR: `target/spoonparser-1.0.0-SNAPSHOT.jar`

## Usage

### Command Line Interface

The analyzer provides three main operation modes:

#### 1. Project Analysis
Analyze a complete Maven project and extract all elements:

```bash
# Analyze a project and output to stdout
mvn exec:java -Dexec.args="--analyze /path/to/maven/project"

# Save analysis to file
mvn exec:java -Dexec.args="--analyze /path/to/maven/project --output analysis.json"

# Compact JSON output
mvn exec:java -Dexec.args="--analyze /path/to/project --format compact-json"
```

#### 2. Version Comparison
Compare two analysis results to detect breaking changes:

```bash
# Compare two versions
mvn exec:java -Dexec.args="--compare old-analysis.json,new-analysis.json"

# Compare with custom version labels
mvn exec:java -Dexec.args="--compare old.json,new.json --old-version 'v1.0' --new-version 'v2.0'"

# Save comparison results
mvn exec:java -Dexec.args="--compare old.json,new.json --output comparison.json"
```

#### 3. Line-based Extraction
Extract elements from specific source code lines:

```bash
# Extract elements from specific lines
mvn exec:java -Dexec.args="--extract-lines src/main/java/Example.java:10,15,20 --project /path/to/project"

# Multiple line ranges
mvn exec:java -Dexec.args="--extract-lines src/main/java/Calculator.java:26,45 --project /path/to/project"
```

### CLI Options

| Option | Description |
|--------|-------------|
| `--analyze <path>` | Analyze a Maven project directory |
| `--compare <old.json,new.json>` | Compare two analysis results |
| `--extract-lines <file:lines>` | Extract elements from specific lines |
| `--output <file>` | Output file path (default: stdout) |
| `--format <format>` | Output format: `json`, `compact-json` |
| `--project <path>` | Project path for line extraction |
| `--old-version <label>` | Label for old version in comparison |
| `--new-version <label>` | Label for new version in comparison |
| `--verbose` | Enable verbose logging |
| `--help` | Show help message |

## Output Format

### Analysis Result Structure

```json
{
  "timestamp": "2025-09-29T11:53:26.609424486Z",
  "projectPath": "/path/to/project",
  "moduleResults": {
    "module-name": {
      "moduleName": "module-name",
      "modulePath": "/path/to/module",
      "elements": [...],
      "sourceFiles": [...],
      "dependencies": [...]
    }
  },
  "elements": [
    {
      "fullyQualifiedName": "com.example.Calculator",
      "type": "CLASS",
      "modifiers": ["public"],
      "lineNumber": 8,
      "sourceFile": "/path/to/Calculator.java"
    },
    {
      "fullyQualifiedName": "com.example.Calculator#add",
      "type": "METHOD",
      "modifiers": ["public"],
      "lineNumber": 26,
      "sourceFile": "/path/to/Calculator.java",
      "parameters": [
        {
          "name": "a",
          "type": "int",
          "fullyQualifiedType": "int",
          "isFinal": false,
          "isVarArgs": false
        }
      ],
      "returnType": "int",
      "signature": "add(int, int) : int"
    }
  ],
  "totalElements": 14,
  "analysisTimeMs": 527
}
```

### Comparison Result Structure

```json
{
  "timestamp": "2025-09-29T11:54:51.154731147Z",
  "oldVersion": "v1.0",
  "newVersion": "v2.0",
  "breakingChanges": [
    {
      "type": "REMOVED_PUBLIC_METHOD",
      "elementFqn": "com.example.Calculator#divide",
      "elementType": "METHOD",
      "description": "Removed public method: com.example.Calculator#divide",
      "severity": "HIGH"
    }
  ],
  "addedElements": [...],
  "removedElements": [...],
  "modifiedElements": [...],
  "summary": {
    "totalBreakingChanges": 1,
    "highSeverityChanges": 1,
    "addedElements": 2,
    "removedElements": 2,
    "isBackwardCompatible": false
  }
}
```

## Element Types

The analyzer extracts the following element types:

- **CLASS**: Java classes with inheritance and interface information
- **INTERFACE**: Java interfaces with method signatures
- **ENUM**: Enumeration types with constants
- **ANNOTATION**: Annotation types with values
- **METHOD**: Methods with parameters, return types, and annotations
- **CONSTRUCTOR**: Constructors with parameter information  
- **FIELD**: Fields with types and modifiers

## Breaking Change Detection

The analyzer identifies the following types of breaking changes:

| Change Type | Description | Severity |
|-------------|-------------|----------|
| `REMOVED_PUBLIC_METHOD` | Public method removed | HIGH |
| `REMOVED_PUBLIC_FIELD` | Public field removed | HIGH |
| `REMOVED_PUBLIC_CLASS` | Public class removed | HIGH |
| `CHANGED_METHOD_SIGNATURE` | Method signature changed | HIGH |
| `CHANGED_RETURN_TYPE` | Method return type changed | HIGH |
| `VISIBILITY_REDUCED` | Access modifier reduced | MEDIUM |
| `MODIFIER_CHANGED` | Other modifiers changed | MEDIUM |
| `ANNOTATION_CHANGED` | Annotations modified | LOW |

## Multi-module Project Support

The analyzer automatically detects and processes multi-module Maven projects:

```
project-root/
├── pom.xml                 # Parent POM with modules
├── module-a/
│   ├── pom.xml
│   └── src/main/java/...
├── module-b/
│   ├── pom.xml
│   └── src/main/java/...
└── shared/
    ├── pom.xml
    └── src/main/java/...
```

Each module is analyzed separately and results are aggregated with individual module reporting.

## Integration Examples

### CI/CD Pipeline Integration

```bash
#!/bin/bash
# Compare current branch with main
mvn exec:java -Dexec.args="--analyze . --output current-analysis.json"
mvn exec:java -Dexec.args="--analyze /path/to/main --output main-analysis.json" 
mvn exec:java -Dexec.args="--compare main-analysis.json,current-analysis.json --output changes.json"

# Check for breaking changes
if grep -q '"isBackwardCompatible": false' changes.json; then
    echo "Breaking changes detected!"
    exit 1
fi
```

### IDE Plugin Integration

```java
// Extract elements at cursor position
SpoonCodeAnalyzer analyzer = new SpoonCodeAnalyzer();
analyzer.analyzeProject(projectPath);
List<ElementInfo> elements = analyzer.extractElementsFromLines(
    sourceFile, Set.of(cursorLine)
);
```

## Testing

Run the test suite:

```bash
mvn test
```

The test suite includes:
- Unit tests for core analyzer functionality
- Integration tests with sample Maven projects
- Comparison engine validation tests
- JSON serialization/deserialization tests

## Technical Architecture

### Core Components

1. **SpoonCodeAnalyzer**: Main analysis engine using Spoon framework
2. **VersionComparator**: Breaking change detection and comparison logic
3. **MavenProjectHandler**: Multi-module project support
4. **JsonOutputGenerator**: Structured output generation
5. **CLI Interface**: Command-line tool implementation

### Dependencies

- **Spoon Framework 10.4.2**: AST parsing and analysis
- **Jackson 2.15.2**: JSON processing
- **Apache Commons CLI 1.5.0**: Command-line interface
- **Maven Model 3.9.4**: Maven project structure handling
- **SLF4J + Logback**: Logging framework

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Examples

See the `/examples` directory for sample projects and usage scenarios demonstrating all features of the Maven Code Analyzer.