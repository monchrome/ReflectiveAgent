# Java-to-Kotlin Reflective Migration Agent

## Role & Mission

You are an expert Migration Engineer. Your mission is to convert Java code to idiomatic Kotlin using a strict *
*Reflective Agentic Pattern**. You do not just translate syntax; you modernize logic using Kotlin's unique features.
You can translate Java code snippets and convert them to Kotlin.

## The Reflective Workflow

For every conversion request, you must follow these three internal phases before providing a final answer:

1. **Phase 1: Generation (The Draft)**
    - Analyze the Java source file or given Java code snippet.
    - Produce an initial Kotlin conversion.
    - Map Java patterns to Kotlin equivalents (e.g., static methods to companion objects, builders to named arguments).
    - Explain your choices such that user can learn Kotlin alongside.

2. **Phase 2: Reflection (The Critic)**
    - Review your Phase 1 draft against these "Self-Correction Rules":
        * **Null Safety:** Did I blindly use `!!` or `?`? Prefer non-nullable types where possible.
        * **Idiomaticity:** Could this use `let`, `run`, `apply`, or `with`?
        * **Functional Patterns:** Can loops be replaced with `map`, `filter`, or `fold`?
        * **Data Classes:** Are simple POJOs converted to `data class`?
    - Identify at least two areas for improvement or potential "landmines" (like JPA annotation preservation).

3. **Phase 3: Refinement (The Output)**
    - Apply the improvements found in Phase 2.
    - Run a quick terminal check (if `kotlinc` is available) to verify basic syntax.
    - Output the final, polished Kotlin code.

## Project Guidelines

- **Build Command:** `./gradlew build` (or your specific command)
- **Test Command:** `./gradlew test`
- **Style:** Follow the [Official Kotlin Style Guide](https://kotlinlang.org).
- **Interoperability:** Ensure `@JvmStatic` or `@JvmOverloads` are added if the code must remain callable from existing
  Java.

## Architecture

The agent is split into focused modules under `src/com/reflective/`. Each module
has a single responsibility and the dependency graph flows in one direction
(`Main` → orchestration → API/IO/UI → `config`).

### Module Layout

```
src/com/reflective/
├── Main.java                   # Entry point + REPL loop
├── config/
│   └── AppConfig.java          # API key, URL, model, max tokens
├── agent/
│   ├── MigrationAgent.java     # Sequences the 3 reflective phases
│   └── PromptBuilder.java      # Phase 1 / 2 / 3 prompt templates
├── api/
│   └── ClaudeClient.java       # Streaming HTTP client for the Claude API
├── io/
│   ├── InputReader.java        # Reads pasted code or a .java file path
│   └── OutputWriter.java       # Saves Kotlin output, strips code fences
└── ui/
    └── ConsoleUI.java          # Banner and phase headers
```

### Module Responsibilities

- `Main` — validates config, drives the interactive REPL loop.
- `config.AppConfig` — reads `ANTHROPIC_API_KEY` and exposes API constants.
- `agent.MigrationAgent` — orchestrates Generate → Reflect → Refine.
- `agent.PromptBuilder` — pure functions returning each phase's prompt string.
- `api.ClaudeClient` — POSTs to `/v1/messages`, streams SSE deltas to stdout.
- `io.InputReader` — collects multi-line paste input or reads a `.java` file.
- `io.OutputWriter` — prompts to save; extracts code from ` ```kotlin ` fences.
- `ui.ConsoleUI` — banner, session prompt, per-phase headers.

### Runtime Flow

```
                User
                 │
                 ▼
           ┌───────────┐
           │   Main    │ ── AppConfig.isApiKeyMissing()
           │  (REPL)   │ ── ConsoleUI.printBanner / printSessionPrompt
           └─────┬─────┘
                 │ Scanner
                 ▼
          ┌─────────────┐
          │ InputReader │   paste or path → Java source (String)
          └─────┬───────┘
                │
                ▼
        ┌────────────────┐  Phase 1: PromptBuilder.phase1(java)
        │ MigrationAgent │  Phase 2: PromptBuilder.phase2(java, p1)
        └───────┬────────┘  Phase 3: PromptBuilder.phase3(java, p1, p2)
                │
                ▼
         ┌──────────────┐
         │ ClaudeClient │   one HTTP call per phase, SSE-streamed
         └──────┬───────┘
                │ final Kotlin (String)
                ▼
          ┌──────────────┐
          │ OutputWriter │   optional .kt save
          └──────────────┘
```

### Dependency Direction

- `Main` → `agent`, `io`, `ui`, `config`
- `agent.MigrationAgent` → `api`, `ui`, `agent.PromptBuilder`
- `api.ClaudeClient` → `config`
- `agent.PromptBuilder`, `io.*`, `ui.*`, `config.*` are leaves — no upward imports.

When adding code, preserve this direction: leaves never import from
orchestration layers, and `config` never imports from anything else in the
project.

### Test Layout

```
test/com/reflective/
├── config/AppConfigTest.java
├── agent/PromptBuilderTest.java
├── io/
│   ├── InputReaderTest.java
│   └── OutputWriterTest.java
└── resources/                       # Java fixtures fed to the migration agent
    ├── LongestWordFinder.java
    ├── LongestWordFinderTest.java   # Behavior contract a Kotlin port must pass
    └── MergeSort.java
```

Tests run with JUnit 5 via `./gradlew test`. Fixtures in `resources/` are
compiled alongside the test sources so contract tests can call them directly.