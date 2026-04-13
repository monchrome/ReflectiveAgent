import com.google.gson.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    private static final String API_KEY  = System.getenv("ANTHROPIC_API_KEY");
    private static final String API_URL  = "https://api.anthropic.com/v1/messages";
    private static final String MODEL    = "claude-sonnet-4-6";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson       GSON        = new Gson();

    // ──────────────────────────────────────────────────────────────────────────
    // Entry point
    // ──────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        printBanner();

        if (API_KEY == null || API_KEY.isBlank()) {
            System.err.println("ERROR: ANTHROPIC_API_KEY environment variable is not set.");
            System.err.println("       export ANTHROPIC_API_KEY=sk-ant-...");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n" + "═".repeat(62));
            System.out.println("  Provide a Java file path  OR  paste Java code below.");
            System.out.println("  End pasted code with a blank line.  Type 'exit' to quit.");
            System.out.println("═".repeat(62));
            System.out.print("\n> ");

            String javaCode = collectInput(scanner);
            if (javaCode == null) break;           // user typed 'exit'
            if (javaCode.isBlank()) {
                System.out.println("No input received — try again.");
                continue;
            }

            try {
                String finalKotlin = runMigration(javaCode);
                offerSave(scanner, finalKotlin);
            } catch (Exception e) {
                System.err.println("\n[Error] " + e.getMessage());
            }
        }

        System.out.println("\nGoodbye!");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Three-phase reflective migration
    // ──────────────────────────────────────────────────────────────────────────

    private static String runMigration(String javaCode) throws Exception {
        printPhaseHeader(1, "Generation", "Producing initial Kotlin conversion...");
        String phase1 = callClaude(buildPhase1Prompt(javaCode));

        printPhaseHeader(2, "Reflection", "Critiquing the draft...");
        String phase2 = callClaude(buildPhase2Prompt(javaCode, phase1));

        printPhaseHeader(3, "Refinement", "Applying improvements for the final output...");
        return callClaude(buildPhase3Prompt(javaCode, phase1, phase2));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Prompt builders
    // ──────────────────────────────────────────────────────────────────────────

    private static String buildPhase1Prompt(String javaCode) {
        return "You are an expert Java-to-Kotlin Migration Engineer.\n\n"
            + "TASK: Produce an initial Kotlin conversion of the Java code below.\n\n"
            + "For each significant conversion decision add a brief inline comment explaining WHY, "
            + "so the user can learn Kotlin alongside the migration. In particular, call out:\n"
            + "  - Static methods/fields  ->  companion objects\n"
            + "  - Builder patterns       ->  named arguments with default values\n"
            + "  - POJOs                  ->  data classes\n"
            + "  - Checked exceptions     ->  Kotlin's unchecked approach\n"
            + "  - Null-handling strategy chosen\n\n"
            + "Java code:\n```java\n" + javaCode + "\n```\n\n"
            + "Output the initial Kotlin conversion with explanatory comments for every key decision.";
    }

    private static String buildPhase2Prompt(String javaCode, String phase1) {
        return "You are a Kotlin code quality reviewer performing a reflective self-critique.\n\n"
            + "Original Java:\n```java\n" + javaCode + "\n```\n\n"
            + "Draft Kotlin conversion to review:\n```kotlin\n" + phase1 + "\n```\n\n"
            + "Review the draft against these Self-Correction Rules and identify **at least two** "
            + "specific improvements or \"landmines\":\n\n"
            + "1. **Null Safety**  - Is `!!` used unnecessarily? Are nullable types (`?`) used where "
            + "non-null is guaranteed?\n"
            + "2. **Idiomaticity** - Could `let`, `run`, `apply`, `also`, or `with` improve clarity?\n"
            + "3. **Functional Patterns** - Can imperative loops become `map`, `filter`, `fold`, or `groupBy`?\n"
            + "4. **Data Classes** - Are all simple POJOs converted to `data class`? Are manual "
            + "`equals`/`hashCode`/`toString` overrides now redundant?\n"
            + "5. **Landmines** - Are JPA/Spring/Jackson annotations preserved? Any Java-interop "
            + "issues requiring `@JvmStatic` or `@JvmOverloads`?\n\n"
            + "For each issue found, state:\n"
            + "  * **Issue:** what is wrong\n"
            + "  * **Location:** which line/block\n"
            + "  * **Fix:** the corrected Kotlin snippet";
    }

    private static String buildPhase3Prompt(String javaCode, String phase1, String phase2) {
        return "You are an expert Kotlin engineer producing the final, polished migration output.\n\n"
            + "Original Java:\n```java\n" + javaCode + "\n```\n\n"
            + "Draft Kotlin - Phase 1:\n```kotlin\n" + phase1 + "\n```\n\n"
            + "Issues and fixes identified in Phase 2:\n" + phase2 + "\n\n"
            + "Apply ALL Phase 2 improvements. Rules for the final output:\n"
            + "  - Idiomatic Kotlin throughout; follow the official Kotlin Style Guide.\n"
            + "  - Preserve every critical annotation (JPA, Spring, Jackson, etc.).\n"
            + "  - Add brief comments only where logic is non-obvious.\n"
            + "  - Output ONLY the final Kotlin code block, no extra prose.";
    }

    // ──────────────────────────────────────────────────────────────────────────
    // API call with streaming output
    // ──────────────────────────────────────────────────────────────────────────

    private static String callClaude(String prompt) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.addProperty("max_tokens", 4096);
        body.addProperty("stream", true);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(userMsg);
        body.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type",      "application/json")
                .header("x-api-key",         API_KEY)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpResponse<InputStream> response =
                HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            try (InputStream errStream = response.body()) {
                String err = new String(errStream.readAllBytes());
                throw new RuntimeException("API error " + response.statusCode() + ": " + err);
            }
        }

        StringBuilder full = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(response.body()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String data = line.substring(6).trim();
                if (data.equals("[DONE]")) break;
                try {
                    JsonObject event = GSON.fromJson(data, JsonObject.class);
                    String type = event.get("type").getAsString();
                    if ("content_block_delta".equals(type)) {
                        JsonObject delta = event.getAsJsonObject("delta");
                        if ("text_delta".equals(delta.get("type").getAsString())) {
                            String text = delta.get("text").getAsString();
                            System.out.print(text);
                            System.out.flush();
                            full.append(text);
                        }
                    }
                } catch (Exception ignored) { /* skip malformed SSE lines */ }
            }
        }
        System.out.println(); // newline after streamed output
        return full.toString();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Input handling
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Reads lines from the scanner until a blank line (signals end of paste).
     * Returns null if the user typed 'exit'.
     * Single-line input that looks like a file path is read from disk.
     */
    private static String collectInput(Scanner scanner) {
        List<String> lines = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // Exit command on the first non-blank line
            if (lines.isEmpty() && line.trim().equalsIgnoreCase("exit")) return null;

            // Blank line ends the paste (only after at least one real line)
            if (line.isBlank() && !lines.isEmpty()) break;

            // Skip leading blank lines
            if (line.isBlank()) continue;

            lines.add(line);
        }

        if (lines.isEmpty()) return "";

        // Single line that looks like a path -> read the file
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

    // ──────────────────────────────────────────────────────────────────────────
    // Save result
    // ──────────────────────────────────────────────────────────────────────────

    private static void offerSave(Scanner scanner, String phase3Response) {
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
    private static String extractKotlinCode(String response) {
        int start = response.indexOf("```kotlin");
        if (start != -1) {
            start = response.indexOf('\n', start) + 1;
            int end = response.indexOf("```", start);
            if (end != -1) return response.substring(start, end).trim();
        }
        // Plain ``` fence
        int plainStart = response.indexOf("```\n");
        if (plainStart != -1) {
            plainStart += 4;
            int end = response.indexOf("```", plainStart);
            if (end != -1) return response.substring(plainStart, end).trim();
        }
        return response.trim();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ──────────────────────────────────────────────────────────────────────────

    private static void printBanner() {
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

    private static void printPhaseHeader(int num, String name, String description) {
        System.out.println("\n+-- Phase " + num + ": " + name + " -- " + description);
        System.out.println("|");
    }
}
