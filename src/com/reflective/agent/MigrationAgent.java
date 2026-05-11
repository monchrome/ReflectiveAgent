package com.reflective.agent;

import com.reflective.api.ClaudeClient;
import com.reflective.ui.ConsoleUI;

public final class MigrationAgent {

    private MigrationAgent() {}

    /** Runs the three reflective phases (Generation -> Reflection -> Refinement). */
    public static String run(String javaCode) throws Exception {
        ConsoleUI.printPhaseHeader(1, "Generation", "Producing initial Kotlin conversion...");
        String phase1 = ClaudeClient.send(PromptBuilder.phase1(javaCode));

        ConsoleUI.printPhaseHeader(2, "Reflection", "Critiquing the draft...");
        String phase2 = ClaudeClient.send(PromptBuilder.phase2(javaCode, phase1));

        ConsoleUI.printPhaseHeader(3, "Refinement", "Applying improvements for the final output...");
        return ClaudeClient.send(PromptBuilder.phase3(javaCode, phase1, phase2));
    }
}