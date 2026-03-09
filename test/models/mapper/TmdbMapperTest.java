package models.mapper;

import models.domain.Movie;
import models.domain.TVShow;
import models.dto.MovieDTO;
import models.dto.TVShowDTO;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class TmdbMapperTest {
    
    @Test
    public void testMapper() {
        // This test is just to ensure that the TmdbMapper class can be instantiated
        TmdbMapper mapper = new TmdbMapper();
        assertNotNull(mapper);
    }

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
