package com.reflective.kotlintest;

import com.reflective.resources.LongestWordFinder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Behavior contract for {@link LongestWordFinder}. A migrated Kotlin
 * implementation is considered a correct conversion when an equivalent
 * Kotlin test (same inputs, same expected outputs) passes against it.
 */
class LongestWordFinderTest {

    @Test
    void returnsEmptyForNullInput() {
        assertEquals("", LongestWordFinder.findLongestWord(null));
    }

    @Test
    void returnsEmptyForEmptyInput() {
        assertEquals("", LongestWordFinder.findLongestWord(""));
    }

    @Test
    void returnsTheOnlyWordForSingleWordInput() {
        assertEquals("solo", LongestWordFinder.findLongestWord("solo"));
    }

    @Test
    void findsLongestAmongManyWords() {
        assertEquals(
            "powerful",
            LongestWordFinder.findLongestWord("Java Stream API makes coding elegant and powerful")
        );
    }

    @Test
    void splitsOnAnyWhitespaceRun() {
        // Tabs, multiple spaces, and newlines all act as a single delimiter.
        assertEquals(
            "longest",
            LongestWordFinder.findLongestWord("a  bb\tccc\n\nlongest mid")
        );
    }

    @Test
    void returnsFirstWordOnLengthTie() {
        // Stream.max keeps the earlier element on a comparator tie; Kotlin's
        // maxByOrNull (strict <) does the same — a correct conversion must
        // preserve this first-wins behavior.
        assertEquals("alpha", LongestWordFinder.findLongestWord("alpha bravo cargo"));
    }

    @Test
    void worksWithSingleCharacterWords() {
        assertEquals("a", LongestWordFinder.findLongestWord("a b c"));
    }
}