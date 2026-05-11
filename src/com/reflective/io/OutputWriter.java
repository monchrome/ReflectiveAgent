package com.reflective.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public final class OutputWriter {

    private OutputWriter() {}

    public static void offerSave(Scanner scanner, String phase3Response) {
        System.out.print("\nSave the Kotlin output to a file? "
                + "(enter filename or press Enter to skip): ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) return;

        if (!name.endsWith(".kt")) name += ".kt";
        String code = extractKotlinCode(phase3Response);
        try {
            Files.writeString(Paths.get(name), code);
            System.out.println("[Saved] " + Paths.get(name).toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Error] Could not save file: " + e.getMessage());
        }
    }

    /** Strips surrounding ```kotlin ... ``` fences if present. */
    static String extractKotlinCode(String response) {
        int start = response.indexOf("```kotlin");
        if (start != -1) {
            start = response.indexOf('\n', start) + 1;
            int end = response.indexOf("```", start);
            if (end != -1) return response.substring(start, end).trim();
        }
        int plainStart = response.indexOf("```\n");
        if (plainStart != -1) {
            plainStart += 4;
            int end = response.indexOf("```", plainStart);
            if (end != -1) return response.substring(plainStart, end).trim();
        }
        return response.trim();
    }
}