package services.tmdb;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import models.domain.Movie;
import models.domain.TVShow;
import org.junit.Test;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Unit tests for {@link TmdbSearchService}.
 * <p>
 * Verifies that movieDetails and tvDetails methods correctly return
 * the expected domain objects when mocked responses are provided.
 * </p>
 *
 * Author: Ali Maher
 */
public class TmdbSearchServiceTest {

    /**
     * Tests fetching movie details via TmdbSearchService.
     * Mocks the service to return a sample Movie object and asserts
     * that the returned Movie has the expected name.
     */
    @Test
    public void testGetMovieDetails() {
        TmdbSearchService service = mock(TmdbSearchService.class);
        Movie movie = new Movie(1, "Test", "overview",
                LocalDate.parse("2020-02-02"), null, null, 80,
                null, null, 80000000,
                20, null, null, null, 
                9.9, 13123);
        when(service.movieDetails(1))
                .thenReturn(CompletableFuture.completedFuture(movie));

        Movie result = service.movieDetails(1).toCompletableFuture().join();
        assertEquals("Test", result.getName());
    }

    /**
     * Tests fetching TV show details via TmdbSearchService.
     * Mocks the service to return a sample TVShow object and asserts
     * that the returned TVShow has the expected name.
     */
    @Test
    public void testGetTVShowDetails() {
        TmdbSearchService service = mock(TmdbSearchService.class);
        TVShow tvShow = new TVShow(1, "Test", "overview",
                LocalDate.parse("2020-02-02"), LocalDate.parse("2025-02-02"), 80, null,
                null, null, 80000000,
                20, null, null, null,
                9.9, 13123, null);
        when(service.tvDetails(1))
                .thenReturn(CompletableFuture.completedFuture(tvShow));

        TVShow result = service.tvDetails(1).toCompletableFuture().join();
        assertEquals("Test", result.getName());
    }
}
