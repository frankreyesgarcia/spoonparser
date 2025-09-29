package com.github.frankreyesgarcia.spoonparser;

/**
 * Main entry point for the Spoon Parser CLI application.
 * This class simply delegates to the actual CLI implementation.
 */
public class SpoonParserCLI {
    
    public static void main(String[] args) {
        com.github.frankreyesgarcia.spoonparser.cli.SpoonParserCLI cli = 
            new com.github.frankreyesgarcia.spoonparser.cli.SpoonParserCLI();
        cli.run(args);
    }
}