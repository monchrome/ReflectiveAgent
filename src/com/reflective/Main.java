package com.reflective;

import com.reflective.agent.MigrationAgent;
import com.reflective.config.AppConfig;
import com.reflective.io.InputReader;
import com.reflective.io.OutputWriter;
import com.reflective.ui.ConsoleUI;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ConsoleUI.printBanner();

        if (AppConfig.isApiKeyMissing()) {
            System.err.println("ERROR: ANTHROPIC_API_KEY environment variable is not set.");
            System.err.println("       export ANTHROPIC_API_KEY=sk-ant-...");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            ConsoleUI.printSessionPrompt();

            String javaCode = InputReader.collect(scanner);
            if (javaCode == null) break;
            if (javaCode.isBlank()) {
                System.out.println("No input received — try again.");
                continue;
            }

            try {
                String finalKotlin = MigrationAgent.run(javaCode);
                OutputWriter.offerSave(scanner, finalKotlin);
            } catch (Exception e) {
                System.err.println("\n[Error] " + e.getMessage());
            }
        }

        System.out.println("\nGoodbye!");
    }
}