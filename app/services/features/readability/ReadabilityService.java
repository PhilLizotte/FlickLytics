package services.features.readability;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Individual task.
 * Service for calculating readability scores. 
 * <p>
 * This class provides implementations of two common readability formulas:
 * <ul>
 *     <li>Flesch Reading Ease</li>
 *     <li>Flesch-Kincaid Grade Level</li>
 * </ul>
 * These metrics estimate how easy or difficult a text is to read based on
 * the number of words, sentences, and syllables.
 * 
 * @author Ali Maher
 */


public class ReadabilityService {

    /**
     * Calculates the <b>Flesch Reading Ease score</b> for a given text.
     * <p>
     * The formula used is:
     *
     * <pre>
     * 206.835 − 1.015 × (words / sentences) − 84.6 × (syllables / words)
     * </pre>
     *
     * Higher scores indicate easier readability.
     *
     * @param text the input text to analyze
     * @return the Flesch Reading Ease score, or {@code 0.0} if the text contains no words
     */
    public double calculateFleschReaddingEase(String text) {
        int totalWords = countWords(text);
        int totalSentences = countSentences(text);
        int totalSyllables = countSyllables(text);

        if (totalWords == 0) {
            return 0.0;
        }

        return 206.835
                - (1.015 * ((double) totalWords / totalSentences))
                - (84.6 * ((double) totalSyllables / totalWords));
    }

    /**
     * Calculates the <b>Flesch-Kincaid Grade Level</b> for a given text.
     * <p>
     * The formula used is:
     *
     * <pre>
     * 0.39 × (words / sentences) + 11.8 × (syllables / words) − 15.59
     * </pre>
     *
     * The result corresponds approximately to a U.S. school grade level
     * required to understand the text.
     *
     * @param text the input text to analyze
     * @return the estimated grade level, or {@code 0.0} if the text contains no words
     */
    public double calculateFleschKincaidGradeLevel(String text) {
        int totalWords = countWords(text);
        int totalSentences = countSentences(text);
        int totalSyllables = countSyllables(text);

        if (totalWords == 0) {
            return 0.0;
        }

        return (0.39 * ((double) totalWords / totalSentences))
                + (11.8 * ((double) totalSyllables / totalWords))
                - 15.59;
    }

    /**
     * Counts the number of words in the provided text.
     * Words are separated using whitespace.
     *
     * @param text the input text
     * @return the number of words found in the text, or {@code 0} if the text is null or empty
     */
    private int countWords (String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Counts the number of sentences in the provided text.
     * Sentences are detected using punctuation marks such as '.', '!', or '?'.
     *
     * @param text the input text
     * @return the number of sentences found in the text, or {@code 0} if the text is null or empty
     */
    private int countSentences (String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] sentences = text.split("[.!?]+");
        return sentences.length;
    }

    /**
     * Estimates the number of syllables in the provided text.
     * <p>
     * The algorithm approximates syllables by:
     * <ul>
     *     <li>Counting groups of vowels (a, e, i, o, u, y)</li>
     *     <li>Subtracting one syllable if the word ends with a silent 'e'</li>
     * </ul>
     *
     * This is a heuristic approach and may not always produce perfectly
     * accurate syllable counts.
     *
     * @param text the input text
     * @return the estimated number of syllables
     */
    private int countSyllables (String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        text = text.toLowerCase().replaceAll("[^a-z]", "");

        int count = 0;

        Matcher matcher = Pattern.compile("[aeiouy]+").matcher(text);

        while (matcher.find()) {
            count++;
        }

        if (text.endsWith("e") && count > 1) {
            count--;
        }

        return count > 0 ? count : 1;
    }
}
