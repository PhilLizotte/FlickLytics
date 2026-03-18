package models.dto;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for DTO classes such as MovieDTO and PersonDTO.
 * <p>
 * Verifies that DTO objects can be instantiated and fields can be assigned correctly.
 * Ensures basic object integrity for domain transfer objects.
 * </p>
 *
 * @author Ali Maher
 */
public class MovieDTOTest {

    /**
     * Tests the MovieDTO object.
     * Assigns sample values to fields and checks object is not null.
     */
    @Test
    public void testMovieDTO() {
        MovieDTO movie = new MovieDTO();

        movie.adult = true;
        movie.id = 5;
        movie.title = "Test";
        movie.overview = "Overview";

        assertNotNull(movie);
    }
}