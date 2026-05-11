package com.reflective.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputWriterTest {

    @Test
    void stripsKotlinFencedBlock() {
        String resp = "Here is the result:\n```kotlin\nfun main() {}\n```\nDone.";
        assertEquals("fun main() {}", OutputWriter.extractKotlinCode(resp));
    }

    @Test
    void stripsPlainFencedBlockWhenNoLanguageTag() {
        String resp = "Result:\n```\nfun main() {}\n```";
        assertEquals("fun main() {}", OutputWriter.extractKotlinCode(resp));
    }

    @Test
    void prefersKotlinFenceWhenBothPresent() {
        String resp = "```\nplain block\n```\n```kotlin\nval x = 1\n```";
        assertEquals("val x = 1", OutputWriter.extractKotlinCode(resp));
    }

    @Test
    void returnsTrimmedOriginalWhenNoFences() {
        assertEquals("just plain text", OutputWriter.extractKotlinCode("  just plain text  "));
    }

    @Test
    void handlesMultilineCodeInsideFence() {
        String resp = "```kotlin\nclass Foo {\n    val x = 1\n}\n```";
        assertEquals("class Foo {\n    val x = 1\n}", OutputWriter.extractKotlinCode(resp));
    }

    @Test
    void returnsTrimmedOriginalWhenFenceIsUnterminated() {
        String resp = "```kotlin\nval x = 1";
        assertEquals("```kotlin\nval x = 1", OutputWriter.extractKotlinCode(resp));
    }
}