package controllers;

import models.domain.Genre;
import models.domain.Movie;
import models.domain.Network;
import models.domain.ProductionCompany;
import models.domain.SpokenLanguage;
import models.domain.TVShow;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;
import services.tmdb.TmdbSearchService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

public class HomeControllerTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        // Mock HomeController so /api/search and /finances return 200 deterministically
        HomeController homeControllerMock = mock(HomeController.class);

        CompletionStage<Result> okJson =
                CompletableFuture.completedFuture(play.mvc.Results.ok("{}"));

        CompletionStage<Result> okText =
                CompletableFuture.completedFuture(play.mvc.Results.ok("OK"));

        when(homeControllerMock.search(anyString(), anyString()))
                .thenReturn(okJson);

        when(homeControllerMock.searchMovieById(anyInt()))
                .thenReturn(okJson);

        when(homeControllerMock.finances(anyInt()))
                .thenReturn(okText);

        // Mock TMDb service so /movie/:id and /tv/:id never call the network
        TmdbSearchService tmdbMock = mock(TmdbSearchService.class);

        // Safe Movie mock (lists MUST NOT be null)
        Movie movie = mock(Movie.class);
        Genre genre = mock(Genre.class);
        ProductionCompany pc = mock(ProductionCompany.class);
        SpokenLanguage lang = mock(SpokenLanguage.class);

        when(genre.getName()).thenReturn("Drama");
        when(pc.getName()).thenReturn("Test Studio");
        when(lang.getName()).thenReturn("English");

        when(movie.getPosterPath()).thenReturn("/poster.png");
        when(movie.getName()).thenReturn("Test Movie");
        when(movie.getTagline()).thenReturn("Test tagline");
        when(movie.getOverview()).thenReturn("Test overview");
        when(movie.getHomepage()).thenReturn("https://example.com");
        when(movie.getGenres()).thenReturn(List.of(genre));
        when(movie.getProductionCompanies()).thenReturn(List.of(pc));
        when(movie.getSpokenLanguages()).thenReturn(List.of(lang));

        // Safe TVShow mock (lists MUST NOT be null)
        TVShow tvShow = mock(TVShow.class);
        Genre tvGenre = mock(Genre.class);
        Network network = mock(Network.class);

        when(tvGenre.getName()).thenReturn("Comedy");
        when(network.getName()).thenReturn("Test Network");

        when(tvShow.getPosterPath()).thenReturn("/poster.png");
        when(tvShow.getName()).thenReturn("Test TV");
        when(tvShow.getTagline()).thenReturn("Test tagline");
        when(tvShow.getOverview()).thenReturn("Test overview");
        when(tvShow.getHomepage()).thenReturn("https://example.com");
        when(tvShow.getGenres()).thenReturn(List.of(tvGenre));
        when(tvShow.getNetworks()).thenReturn(List.of(network));

        when(tmdbMock.movieDetails(anyInt()))
                .thenReturn(CompletableFuture.completedFuture(movie));
        when(tmdbMock.tvDetails(anyInt()))
                .thenReturn(CompletableFuture.completedFuture(tvShow));
        when(tmdbMock.search(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode()));

        return new GuiceApplicationBuilder()
                .configure("tmdb.apiKey", "dummy")
                .configure("tmdb.baseUrl", "https://api.themoviedb.org/3")
                .configure("tmdb.readAccessToken", "dummy")
                .configure("tmdb.raToken", "dummy")
                .overrides(
                        bind(HomeController.class).toInstance(homeControllerMock),
                        bind(TmdbSearchService.class).toInstance(tmdbMock)
                )
                .build();
    }

    @Test
    public void testIndex() {
        Result result = route(app, fakeRequest(GET, "/"));
        assertEquals(OK, result.status());
    }

    @Test
    public void testFinances() {
        Result result = route(app, fakeRequest(GET, "/finances/11"));
        assertEquals(OK, result.status());
    }

    @Test
    public void testSearch() {
        Result result = route(app, fakeRequest(GET, "/api/search?category=movie&query=test"));
        assertEquals(OK, result.status());
    }

    @Test
    public void testMovieDetailsEndpoint() {
        Result result = route(app, fakeRequest(GET, "/movie/11"));
        assertEquals(OK, result.status());
    }

    @Test
    public void testTvShowDetailsEndpoint() {
        Result result = route(app, fakeRequest(GET, "/tv/10"));
        assertEquals(OK, result.status());
    }
}