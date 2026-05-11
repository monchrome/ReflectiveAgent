package com.reflective.agent;

public final class PromptBuilder {

    private PromptBuilder() {}

    public static String phase1(String javaCode) {
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

    public static String phase2(String javaCode, String phase1) {
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

    public static String phase3(String javaCode, String phase1, String phase2) {
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
}