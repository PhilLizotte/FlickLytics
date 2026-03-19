package models.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for domain model class: SearchResult.
 * <p>
 * This test verifies that the constructor correctly assigns all fields
 * and that the getters return the expected values.
 * </p>
 *
 * @author Ali Maher
 */
public class SearchResultTest {

    /**
     * Tests the SearchResult domain object.
     * Verifies that all fields are correctly assigned via the constructor.
     */
    @Test
    public void testSearchResultDomain() {

        SearchResult result = new SearchResult(
                100,
                "Inception",
                "movie",
                "en",
                "2010-07-16",
                95.5,
                8.8,
                "/inception.jpg"
        );

        assertEquals(100, result.getId());
        assertEquals("Inception", result.getTitle());
        assertEquals("movie", result.getMediaType());
        assertEquals("en", result.getLanguage());
        assertEquals("2010-07-16", result.getReleaseDate());
        assertEquals(95.5, result.getPopularity(), 0.01);
        assertEquals(8.8, result.getVoteAverage(), 0.01);
        assertEquals("/inception.jpg", result.getImagePath());
    }
}