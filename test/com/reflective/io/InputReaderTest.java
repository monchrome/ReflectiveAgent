package com.reflective.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InputReaderTest {

    private static Scanner scannerOf(String input) {
        return new Scanner(input);
    }

    @Test
    void returnsNullWhenUserTypesExit() {
        assertNull(InputReader.collect(scannerOf("exit\n")));
    }

    @Test
    void exitIsCaseInsensitive() {
        assertNull(InputReader.collect(scannerOf("EXIT\n")));
        assertNull(InputReader.collect(scannerOf("  Exit  \n")));
    }

    @Test
    void returnsEmptyStringWhenInputIsEmpty() {
        assertEquals("", InputReader.collect(scannerOf("")));
    }

    @Test
    void joinsPastedLinesUntilBlankLine() {
        String input = "public class Foo {\nint x;\n}\n\n";
        assertEquals("public class Foo {\nint x;\n}", InputReader.collect(scannerOf(input)));
    }

    @Test
    void skipsLeadingBlankLines() {
        String input = "\n\nclass Foo {}\n\n";
        assertEquals("class Foo {}", InputReader.collect(scannerOf(input)));
    }

    @Test
    void treatsMissingFilePathAsSnippet() {
        String missingPath = "/definitely/does/not/exist/Foo.java";
        String input = missingPath + "\n\n";
        assertEquals(missingPath, InputReader.collect(scannerOf(input)));
    }

    @Test
    void readsContentsWhenPathPointsToRealJavaFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("Foo.java");
        Files.writeString(file, "class Foo {}");
        String input = file.toString() + "\n\n";
        assertEquals("class Foo {}", InputReader.collect(scannerOf(input)));
    }
}