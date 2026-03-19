package models.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for domain model classes:
 * TVShow.
 * <p>
 * Each test verifies that the constructors correctly assign all fields
 * and that the getters return the expected values.
 * This ensures the integrity of the domain layer.
 * </p>
 *
 * @author Ali Maher
 */
public class TVShowTest {

    /**
     * Tests the TVShow domain object.
     * Verifies that all fields are correctly assigned via the constructor.
     * Uses a TVShow instance with sample data.
     */
    @Test
    public void testTVShowDomain() {
        TVShow movie = new TVShow(
                1,
                "Friends",
                "XXXX",
                LocalDate.of(2008, 7, 18),
                LocalDate.of(2008, 7, 18),
                8.8,
                "https://example.com",
                List.of(new Genre(1, "Action")),
                "/poster.jpg",
                20,
                2,
                List.of(new Network(1, "NBC", "NBC", "NBC")),
                "ok",
                "Why so serious?",
                9.9,
                20,
                "xxx"
        );

        assertEquals(1, movie.getId());
        assertEquals("Friends", movie.getName());
        assertEquals("XXXX", movie.getOverview());
        assertEquals(LocalDate.of(2008, 7, 18), movie.getFirstAirDate());
        assertEquals(LocalDate.of(2008, 7, 18), movie.getLastAirDate());
        assertEquals(8.8, movie.getPopularity(), 0.01);
        assertEquals("https://example.com", movie.getPosterPath());
        assertEquals(1, movie.getGenres().size());
        assertEquals("/poster.jpg", movie.getHomepage());
        assertEquals(20, movie.getNumberOfEpisodes());
        assertEquals(2, movie.getNumberOfSeasons());
        assertEquals(1, movie.getNetworks().size());
        assertEquals("ok", movie.getStatus());
        assertEquals("Why so serious?", movie.getTagline());
        assertEquals(9.9, movie.getVoteAverage(), 0.01);
        assertEquals(20, movie.getVoteCount());
        assertEquals("xxx", movie.getType());
    }

}
