package models.mapper;

import models.domain.Movie;
import models.domain.TVShow;
import models.dto.MovieDTO;
import models.dto.TVShowDTO;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TmdbMapper}.
 * <p>
 * Verifies that the mapper correctly converts DTOs (MovieDTO and TVShowDTO)
 * into their corresponding domain objects (Movie and TVShow).
 * </p>
 *
 * Author: Ali Maher
 */
public class TmdbMapperTest {

    /**
     * Tests converting a MovieDTO to a Movie domain object.
     * Ensures that all relevant fields are mapped correctly.
     */
    @Test
    public void testToMovie() {
        MovieDTO dto = new MovieDTO();
        dto.id = 10;
        dto.title = "Inception";
        dto.overview = "A mind bending movie";
        dto.release_date = LocalDate.parse("2010-07-16");
        dto.vote_average = 8.8;

        Movie movie = TmdbMapper.toMovie(dto);

        assertEquals(10, movie.getId());
        assertEquals("Inception", movie.getName());
        assertEquals("A mind bending movie", movie.getOverview());
        assertEquals(LocalDate.parse("2010-07-16"), movie.getReleaseDate());
        assertEquals(8.8, movie.getVoteAverage(), 0.01);
    }

    /**
     * Tests converting a TVShowDTO to a TVShow domain object.
     * Ensures that all relevant fields are mapped correctly.
     */
    @Test
    public void testToTVShow() {
        TVShowDTO dto = new TVShowDTO();
        dto.id = 10;
        dto.name = "Friends";
        dto.overview = "A popular sitcom";
        dto.vote_average = 8.8;

        TVShow tvShow = TmdbMapper.toTVShow(dto);

        assertEquals(10, tvShow.getId());
        assertEquals("Friends", tvShow.getName());
        assertEquals("A popular sitcom", tvShow.getOverview());
        assertEquals(8.8, tvShow.getVoteAverage(), 0.01);
    }
}