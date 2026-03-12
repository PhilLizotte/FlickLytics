package services.features.readability;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Individual task.
 * Service for calculating readability scores. 
 *
 * @author Ali Maher
 */


public class ReadabilityService {

    public double calculateFleschReaddingEase(String text) {
        int totalWords = countWords(text);
        int totalSentences = countSentences(text);
        int totalSyllables = countSyllables(text);

        if (totalSentences == 0 || totalWords == 0) {
            return 0.0;
        }

        return 206.835
                - (1.015 * ((double) totalWords / totalSentences))
                - (84.6 * ((double) totalSyllables / totalWords));
    }
    
    public double calculateFleschKincaidGradeLevel(String text) {
        int totalWords = countWords(text);
        int totalSentences = countSentences(text);
        int totalSyllables = countSyllables(text);

        if (totalSentences == 0 || totalWords == 0) {
            return 0.0;
        }

        return (0.39 * ((double) totalWords / totalSentences))
                + (11.8 * ((double) totalSyllables / totalWords))
                - 15.59;
    }

    private int countWords (String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    private int countSentences (String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] sentences = text.split("[.!?]+");
        return sentences.length;
    }

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
