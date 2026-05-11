package com.reflective.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptBuilderTest {

    @Test
    void phase1EmbedsJavaSourceVerbatim() {
        String src = "public class Foo { int x; }";
        String prompt = PromptBuilder.phase1(src);
        assertTrue(prompt.contains(src), "phase 1 prompt must embed the Java source");
    }

    @Test
    void phase1CallsOutKotlinIdioms() {
        String prompt = PromptBuilder.phase1("class Foo {}");
        assertTrue(prompt.contains("companion objects"));
        assertTrue(prompt.contains("named arguments"));
        assertTrue(prompt.contains("data classes"));
        assertTrue(prompt.contains("Null-handling"));
    }

    @Test
    void phase2EmbedsBothJavaAndDraftKotlin() {
        String prompt = PromptBuilder.phase2("JAVA_SENTINEL", "KOTLIN_DRAFT_SENTINEL");
        assertTrue(prompt.contains("JAVA_SENTINEL"));
        assertTrue(prompt.contains("KOTLIN_DRAFT_SENTINEL"));
    }

    @Test
    void phase2ListsAllFiveSelfCorrectionRules() {
        String prompt = PromptBuilder.phase2("", "");
        assertTrue(prompt.contains("Null Safety"));
        assertTrue(prompt.contains("Idiomaticity"));
        assertTrue(prompt.contains("Functional Patterns"));
        assertTrue(prompt.contains("Data Classes"));
        assertTrue(prompt.contains("Landmines"));
    }

    @Test
    void phase3IncludesJavaDraftAndReview() {
        String prompt = PromptBuilder.phase3("J_SRC", "K_DRAFT", "REVIEW_BODY");
        assertTrue(prompt.contains("J_SRC"));
        assertTrue(prompt.contains("K_DRAFT"));
        assertTrue(prompt.contains("REVIEW_BODY"));
    }

    @Test
    void phase3DemandsOnlyFinalCodeBlock() {
        String prompt = PromptBuilder.phase3("", "", "");
        assertTrue(prompt.contains("Output ONLY the final Kotlin code block"));
    }
}