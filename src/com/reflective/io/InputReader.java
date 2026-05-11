package com.reflective.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class InputReader {

    private InputReader() {}

    /**
     * Reads lines from the scanner until a blank line (signals end of paste).
     * Returns null if the user typed 'exit'.
     * Single-line input that looks like a file path is read from disk.
     */
    public static String collect(Scanner scanner) {
        List<String> lines = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (lines.isEmpty() && line.trim().equalsIgnoreCase("exit")) return null;

            if (line.isBlank() && !lines.isEmpty()) break;
            if (line.isBlank()) continue;

            lines.add(line);
        }

        if (lines.isEmpty()) return "";

        if (lines.size() == 1 && looksLikeFilePath(lines.get(0).trim())) {
            return readJavaFile(lines.get(0).trim());
        }

        return String.join("\n", lines);
    }

    private static boolean looksLikeFilePath(String s) {
        return s.endsWith(".java")
                || s.startsWith("/")
                || s.startsWith("./")
                || s.startsWith("../")
                || s.startsWith("~");
    }

    private static String readJavaFile(String path) {
        if (path.startsWith("~")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            System.err.println("[Warning] File not found: " + path
                    + " — treating input as a code snippet.");
            return path;
        }
        try {
            String content = Files.readString(p);
            System.out.println("[Info] Read " + Files.size(p)
                    + " bytes from \"" + p.getFileName() + "\"");
            return content;
        } catch (IOException e) {
            System.err.println("[Error] Could not read file: " + e.getMessage());
            return "";
        }
    }
}