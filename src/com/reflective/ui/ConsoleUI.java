package com.reflective.ui;

public final class ConsoleUI {

    private ConsoleUI() {}

    public static void printBanner() {
        System.out.println(
            "╔══════════════════════════════════════════════════════════╗\n"
          + "║       Java  ->  Kotlin  Reflective Migration Agent       ║\n"
          + "║                                                          ║\n"
          + "║   Phase 1 · Generate   ->  initial conversion            ║\n"
          + "║   Phase 2 · Reflect    ->  self-critique                 ║\n"
          + "║   Phase 3 · Refine     ->  polished final output         ║\n"
          + "╚══════════════════════════════════════════════════════════╝"
        );
    }

    public static void printPhaseHeader(int num, String name, String description) {
        System.out.println("\n+-- Phase " + num + ": " + name + " -- " + description);
        System.out.println("|");
    }

    public static void printSessionPrompt() {
        System.out.println("\n" + "═".repeat(62));
        System.out.println("  Provide a Java file path  OR  paste Java code below.");
        System.out.println("  End pasted code with a blank line.  Type 'exit' to quit.");
        System.out.println("═".repeat(62));
        System.out.print("\n> ");
    }
}