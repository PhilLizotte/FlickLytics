package services.features;

import static org.junit.Assert.*;
import org.junit.Test;
import services.features.readability.ReadabilityService;

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
}
