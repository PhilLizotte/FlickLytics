package models.dto;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for known-for/person-stats DTOs.
 * <p>
 * Verifies that PersonKnownForItemDTO, PersonKnownForStatsDTO, and PersonKnownForPageDTO
 * can be instantiated and that their getters return expected values.
 * </p>
 *
 * @author Aram Zand
 */
public class PersonKnownForDTOsTest {

    @Test
    public void testPersonKnownForItemDTOConstructorAndGetters() {
        PersonKnownForItemDTO item = new PersonKnownForItemDTO(
                1,
                "movie",
                "Test Title",
                "2000-01-01",
                10.5,
                7.2,
                123,
                "https://www.themoviedb.org/movie/1"
        );

        assertNotNull(item);
        assertEquals(Integer.valueOf(1), item.getId());
        assertEquals("movie", item.getMediaType());
        assertEquals("Test Title", item.getTitle());
        assertEquals("2000-01-01", item.getReleaseDate());
        assertEquals(10.5, item.getPopularity(), 0.0001);
        assertEquals(7.2, item.getVoteAverage(), 0.0001);
        assertEquals(Integer.valueOf(123), item.getVoteCount());
        assertEquals("https://www.themoviedb.org/movie/1", item.getTmdbUrl());
    }

    @Test
    public void testPersonKnownForStatsDTOConstructorAndGetters() {
        PersonKnownForStatsDTO stats = new PersonKnownForStatsDTO(3, 1.0, 5.0, 3.0);

        assertNotNull(stats);
        assertEquals(3, stats.getCount());
        assertEquals(1.0, stats.getMin(), 0.0001);
        assertEquals(5.0, stats.getMax(), 0.0001);
        assertEquals(3.0, stats.getAverage(), 0.0001);
    }

    @Test
    public void testPersonKnownForPageDTOConstructorAndGetters() {
        PersonKnownForItemDTO item = new PersonKnownForItemDTO(
                1,
                "movie",
                "Test Title",
                "2000-01-01",
                10.5,
                7.2,
                123,
                "https://www.themoviedb.org/movie/1"
        );

        PersonKnownForStatsDTO popularityStats = new PersonKnownForStatsDTO(1, 10.5, 10.5, 10.5);
        PersonKnownForStatsDTO voteAverageStats = new PersonKnownForStatsDTO(1, 7.2, 7.2, 7.2);
        PersonKnownForStatsDTO voteCountStats = new PersonKnownForStatsDTO(1, 123.0, 123.0, 123.0);

        PersonKnownForPageDTO page = new PersonKnownForPageDTO(
                List.of(item),
                popularityStats,
                voteAverageStats,
                voteCountStats
        );

        assertNotNull(page);
        assertEquals(1, page.getItems().size());
        assertEquals(Integer.valueOf(1), page.getItems().get(0).getId());
        assertEquals(1, page.getPopularityStats().getCount());
        assertEquals(1, page.getVoteAverageStats().getCount());
        assertEquals(1, page.getVoteCountStats().getCount());
    }
}
