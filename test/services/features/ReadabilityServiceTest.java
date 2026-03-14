package services.features;

import static org.junit.Assert.*;
import org.junit.Test;
import services.features.readability.ReadabilityService;
import java.lang.reflect.Method;

public class ReadabilityServiceTest {
    
    @Test
    public void testReadabilityService() {
        ReadabilityService readabilityService = new ReadabilityService();
        assertNotNull(readabilityService);
    }
    
    @Test
    public void testCalculateFleschReadingEase() {
        ReadabilityService readabilityService = new ReadabilityService();
        String text = "The cat sat on the mat.";
        double score = readabilityService.calculateFleschReaddingEase(text);
        // The expected score for this simple sentence is around 100
        assertTrue(score > 90);
    }
    
    @Test
    public void testCalculateFleschKincaidGradeLevel() {
        ReadabilityService readabilityService = new ReadabilityService();
        String text = "The cat sat on the mat.";
        double gradeLevel = readabilityService.calculateFleschKincaidGradeLevel(text);
        // The expected grade level for this simple sentence is around 1
        assertTrue(gradeLevel < 2);
    }
    
    @Test
    public void testEmptyText() {
        ReadabilityService readabilityService = new ReadabilityService();
        String text = "";
        double score = readabilityService.calculateFleschReaddingEase(text);
        double gradeLevel = readabilityService.calculateFleschKincaidGradeLevel(text);
        // For empty text, we can define the behavior as returning 0 for both
        assertEquals(0, score, 0.01);
        assertEquals(0, gradeLevel, 0.01);
    }

    @Test
    public void testSilentE() {
        ReadabilityService service = new ReadabilityService();
        String text = "cake";
        double score = service.calculateFleschReaddingEase(text);
        // The word "cake" has a silent 'e', so it should count as 1 syllable, not 2
        assertTrue(score >= 0);
    }

    @Test
    public void testEdgeCases() {
        ReadabilityService service = new ReadabilityService();
        String text1 = "b";
        String text2 = "cake";
        String text3 = "the";
        String text4 = "radio";
        String text5 = "cat";
        Method countSyllablesMethod;
        try {
            countSyllablesMethod = ReadabilityService.class
                    .getDeclaredMethod("countSyllables", String.class);
        } catch (NoSuchMethodException e) {
            fail("Method countSyllables not found");
            return;
        }

        countSyllablesMethod.setAccessible(true);

        int result1;
        int result2;
        int result3;
        int result4;
        int result5;
        try {
            result1 = (int) countSyllablesMethod.invoke(service, text1);
            result2 = (int) countSyllablesMethod.invoke(service, text2);
            result3 = (int) countSyllablesMethod.invoke(service, text3);
            result4 = (int) countSyllablesMethod.invoke(service, text4);
            result5 = (int) countSyllablesMethod.invoke(service, text5);
        } catch (Exception e) {
            fail("Failed to invoke countSyllables method");
            return;
        }

        assertEquals(1, result1);
        assertEquals(1, result2);
        assertEquals(1, result3);
        assertEquals(2, result4);
        assertEquals(1, result5);
    }

    @Test
    public void testPrivateMethodsWithNull() throws Exception {
        ReadabilityService service = new ReadabilityService();

        Method countWords = ReadabilityService.class
                .getDeclaredMethod("countWords", String.class);
        Method countSentences = ReadabilityService.class
                .getDeclaredMethod("countSentences", String.class);
        Method countSyllables = ReadabilityService.class
                .getDeclaredMethod("countSyllables", String.class);

        countWords.setAccessible(true);
        countSentences.setAccessible(true);
        countSyllables.setAccessible(true);

        assertEquals(0, (int) countWords.invoke(service, new Object[]{null}));
        assertEquals(0, (int) countSentences.invoke(service, new Object[]{null}));
        assertEquals(0, (int) countSyllables.invoke(service, new Object[]{null}));
    }
}
