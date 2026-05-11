can# Java → Kotlin Reflective Migration Agent

A CLI tool that converts Java source code to idiomatic Kotlin using a **three-phase reflective agentic pattern** powered by the Claude API. As of now , because I have expired my Codex tokens , this tool is using the same model for "Reflect and Refinement" step too. Ideally, would prefer two different models to be used for "Generation" and "Reflect and Refinement" step.

## How It Works

The agent doesn't just translate syntax — it runs three consecutive Claude API calls, each building on the last:

| Phase | Name | What happens |
|-------|------|--------------|
| 1 | **Generate** | Produces an initial Kotlin conversion with inline comments explaining each decision |
| 2 | **Reflect** | Critiques the draft against self-correction rules, identifying at least two issues or "landmines" |
| 3 | **Refine** | Applies all Phase 2 improvements and outputs the final, polished Kotlin |

All three phases stream output to the terminal in real time.

## Prerequisites

- Java 17+
- An [Anthropic API key](https://console.anthropic.com/)

## Setup

```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

## Build & Run

Build a self-contained fat JAR:

```bash
./gradlew shadowJar
```

Run the agent:

```bash
java -jar build/libs/reflective-agent.jar
```

## Usage

Once running, you have two input modes:

**File path** — enter a path to a `.java` file:
```
> /path/to/MyClass.java
> ./src/MyClass.java
> ~/projects/MyClass.java
```

**Paste code** — paste Java code directly, then press Enter on a blank line to submit:
```
> public class Foo {
>     private String name;
>     public String getName() { return name; }
> }
>
```

After migration, you'll be prompted to optionally save the output as a `.kt` file.

Type `exit` to quit.

## Self-Correction Rules (Phase 2)

The reflection phase checks for:

- **Null Safety** — unnecessary `!!` or overly broad `?` usage
- **Idiomaticity** — missed opportunities for `let`, `run`, `apply`, `also`, `with`
- **Functional Patterns** — imperative loops that could be `map`, `filter`, `fold`, `groupBy`
- **Data Classes** — POJOs not yet converted, redundant `equals`/`hashCode`/`toString`
- **Landmines** — dropped JPA/Spring/Jackson annotations, missing `@JvmStatic` / `@JvmOverloads`

## Project Structure

The code is organized into focused modules under `src/com/reflective/`:

```
src/com/reflective/
├── Main.java                   # Entry point + REPL loop
├── config/
│   └── AppConfig.java          # API key, URL, model constants
├── agent/
│   ├── MigrationAgent.java     # Orchestrates the 3 reflective phases
│   └── PromptBuilder.java      # Phase 1 / 2 / 3 prompt templates
├── api/
│   └── ClaudeClient.java       # Streaming HTTP client for the Claude API
├── io/
│   ├── InputReader.java        # Reads pasted code or a .java file path
│   └── OutputWriter.java       # Saves Kotlin output, strips code fences
└── ui/
    └── ConsoleUI.java          # Banner and phase headers

build.gradle.kts                # Gradle build (Java 17, Shadow JAR)
settings.gradle.kts
```

### Module responsibilities

| Module | Responsibility |
|--------|----------------|
| `Main` | Validates config, runs the interactive REPL loop |
| `config.AppConfig` | Reads `ANTHROPIC_API_KEY` and exposes API constants |
| `agent.MigrationAgent` | Sequences the Generate → Reflect → Refine phases |
| `agent.PromptBuilder` | Builds the prompt string for each phase |
| `api.ClaudeClient` | Posts to `/v1/messages` and streams SSE deltas to stdout |
| `io.InputReader` | Collects multi-line paste input or reads a `.java` file |
| `io.OutputWriter` | Prompts the user to save and extracts code from fenced blocks |
| `ui.ConsoleUI` | Banner, session prompt, and per-phase headers |

## Dependencies

- [Gson](https://github.com/google/gson) `2.10.1` — JSON serialization for API requests
- [Shadow](https://github.com/GradleUp/shadow) `9.0.0` — fat JAR packaging
