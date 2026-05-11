package com.reflective.resources;

import java.util.Arrays;
import java.util.Comparator;

public class LongestWordFinder {
    /**
     * Finds the longest word in a given sentence using Java Streams.
     * @param sentence The input string to search.
     * @return The longest word found, or an empty string if the input is null or empty.
     */
    public static String findLongestWord(String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return "";
        }

        return Arrays.stream(sentence.split("\\s+")) // Split by whitespace
                .max(Comparator.comparingInt(String::length)) // Find max by length
                .orElse(""); // Handle empty stream case
    }

    public static void main(String[] args) {
        String input = "Java Stream API makes coding elegant and powerful";
        String result = findLongestWord(input);
        System.out.println("Longest word: " + result); // Output: powerful
    }
}
