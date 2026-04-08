package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.Before;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import services.features.diversity.GlobalDiversityService;
import services.features.financial.FinancialPerformanceService;
import services.features.personstats.PersonStatsService;
import services.features.readability.ReadabilityService;
import services.tmdb.TmdbSearchService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import models.domain.Genre;
import models.domain.Movie;
import models.domain.ProductionCompany;
import models.domain.SpokenLanguage;
import models.domain.TVShow;
import models.dto.PersonKnownForItemDTO;
import models.dto.PersonKnownForPageDTO;
import models.dto.PersonKnownForStatsDTO;
import org.mockito.Mockito;

import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.*;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.inject.Bindings.bind;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link controllers.TmdbSearchController}.
 * <p>
 * Verifies controller endpoints return expected HTTP status codes for success
 * and failure cases,
 * including the person known-for endpoint.
 * </p>
 *
 * @author team-2
 */
public class TmdbSearchControllerTest extends WithApplication {

        @Override
        protected Application provideApplication() {
                TmdbSearchService tmdbSearchService = Mockito.mock(TmdbSearchService.class);
                FinancialPerformanceService financialPerformanceService = Mockito
                                .mock(FinancialPerformanceService.class);
                PersonStatsService personStatsService = Mockito.mock(PersonStatsService.class);
                ReadabilityService readabilityService = Mockito.mock(ReadabilityService.class);

                Movie dummyMovie = Mockito.mock(Movie.class);
                Mockito.when(dummyMovie.getOverview()).thenReturn("Test overview");
                Mockito.when(dummyMovie.getPosterPath()).thenReturn("/test.jpg");
                Mockito.when(dummyMovie.getName()).thenReturn("Test Movie");
                Mockito.when(dummyMovie.getTagline()).thenReturn("Test tagline");
                Mockito.when(dummyMovie.getReleaseDate()).thenReturn(LocalDate.of(2000, 1, 1));
                Mockito.when(dummyMovie.getGenres()).thenReturn(List.of(new Genre(1, "Drama")));
                Mockito.when(dummyMovie.getHomepage()).thenReturn("https://example.com");
                Mockito.when(dummyMovie.getPopularity()).thenReturn(0.0);
                Mockito.when(dummyMovie.getProductionCompanies())
                                .thenReturn(List.of(new ProductionCompany(1, "", "TestCo", "US")));
                Mockito.when(dummyMovie.getRevenue()).thenReturn(0L);
                Mockito.when(dummyMovie.getRuntime()).thenReturn(0);
                Mockito.when(dummyMovie.getSpokenLanguages()).thenReturn(List.of(new SpokenLanguage(1, "English")));
                Mockito.when(dummyMovie.getStatus()).thenReturn("Released");
                Mockito.when(dummyMovie.getVoteAverage()).thenReturn(0.0);
                Mockito.when(dummyMovie.getVoteCount()).thenReturn(0);

                TVShow dummyTvShow = Mockito.mock(TVShow.class);
                Mockito.when(dummyTvShow.getOverview()).thenReturn("Test overview");

                Mockito.when(tmdbSearchService.search(Mockito.anyString(), Mockito.anyString()))
                                .thenReturn(CompletableFuture.completedFuture(play.libs.Json.newObject()));
                Mockito.when(tmdbSearchService.movieDetails(Mockito.anyInt()))
                                .thenReturn(CompletableFuture.completedFuture(dummyMovie));
                Mockito.when(tmdbSearchService.tvDetails(Mockito.anyInt()))
                                .thenReturn(CompletableFuture.completedFuture(dummyTvShow));

                ObjectMapper mapper = new ObjectMapper();
                ObjectNode mockMovie = mapper.createObjectNode();
                mockMovie.put("id", 42069);
                mockMovie.put("title", "Sharknado 35");
                mockMovie.put("budget", 1000);
                mockMovie.put("revenue", 150000);
                Mockito.when(financialPerformanceService.getMovieFinances(Mockito.anyInt()))
                                .thenReturn(CompletableFuture.completedFuture(mockMovie));

                PersonKnownForPageDTO dummyKnownForPage = new PersonKnownForPageDTO(
                                java.util.List.of(new PersonKnownForItemDTO(1, "movie", "Test", "2000-01-01", 1.0, 1.0,
                                                1, "https://example.com")),
                                new PersonKnownForStatsDTO(1, 1.0, 1.0, 1.0),
                                new PersonKnownForStatsDTO(1, 1.0, 1.0, 1.0),
                                new PersonKnownForStatsDTO(1, 1.0, 1.0, 1.0));
                Mockito.when(personStatsService.getKnownForPage(Mockito.anyInt()))
                                .thenReturn(CompletableFuture.completedFuture(dummyKnownForPage));
                Mockito.when(readabilityService.calculateFleschReaddingEase(Mockito.anyString())).thenReturn(0.0);
                Mockito.when(readabilityService.calculateFleschKincaidGradeLevel(Mockito.anyString())).thenReturn(0.0);

                return new GuiceApplicationBuilder()
                                .configure(Map.of(
                                                "tmdb.apiKey", "test-api-key",
                                                "tmdb.raToken", "test-ra-token"))
                                .overrides(
                                                bind(TmdbSearchService.class).toInstance(tmdbSearchService),
                                                bind(FinancialPerformanceService.class)
                                                                .toInstance(financialPerformanceService),
                                                bind(PersonStatsService.class).toInstance(personStatsService),
                                                bind(ReadabilityService.class).toInstance(readabilityService))
                                .build();
        }

        @Test
        public void testIndex() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/");

                Result result = route(app, request);
                assertEquals(OK, result.status());
        }

        @Test
        public void testFinances() {
                Result result = route(app,
                        fakeRequest(GET, "/finances/11"));
                assertEquals(OK, result.status());
        }

        @Test
        public void testSearch() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/api/search?category=movie&query=test");

                Result result = route(app, request);
                assertEquals(OK, result.status());
        }

        @Test
        public void testSearchNullCategory() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/api/search?category=&query=test");

                Result result = route(app, request);
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void testSearchNullQuery() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/api/search?category=movie&query=");

                Result result = route(app, request);
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void testSearchMissingCategoryReturnsBadRequest() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/api/search?query=test");

                Result result = route(app, request);
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void testSearchMissingQueryReturnsBadRequest() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/api/search?category=movie");

                Result result = route(app, request);
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void testKnownForEndpoint() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/person/500/known-for");

                Result result = route(app, request);
                assertEquals(OK, result.status());
        }

        @Test
        public void testReview() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/reviews/movie/1/fake");

                Result result = route(app, request);
                assertEquals(OK, result.status());
        }

        @Test
        public void testGlobalDiversity() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/diversity/movie/1");

                Result result = route(app, request);
                assertEquals(OK, result.status());
        }

        private TmdbSearchController controller;

        @Before
        public void setup() {
                GlobalDiversityService service = mock(GlobalDiversityService.class);
                ActorSystem actorSystem = ActorSystem.create("test-system");
                controller = new TmdbSearchController(null, null, null, null, service, null, actorSystem);
        }

        @Test
        public void shouldReturnBadRequest_whenCategoryIsBlank() throws Exception {
                Result result = controller.globalDiversity("   ", 1).toCompletableFuture().get();

                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void shouldReturnBadRequest_whenCategoryIsNull() throws Exception {
                Result result = controller.globalDiversity(null, 1).toCompletableFuture().get();

                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void shouldReturnBadRequest_whenCategoryIsNull_2() throws Exception {
                Result result = controller.search(null, "query").toCompletableFuture().get();
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void shouldReturnBadRequest_whenCategoryIsBlank_2() throws Exception {
                Result result = controller.search("   ", "query").toCompletableFuture().get();
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void shouldReturnBadRequest_whenQueryIsNull() throws Exception {
                Result result = controller.search("movie", null).toCompletableFuture().get();
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void shouldReturnBadRequest_whenQueryIsBlank() throws Exception {
                Result result = controller.search("movie", "   ").toCompletableFuture().get();
                assertEquals(BAD_REQUEST, result.status());
        }

        @Test
        public void shouldReturnBadRequest_whenIdIsNull() throws Exception {
                Result result = controller.globalDiversity("movie", null).toCompletableFuture().get();

                assertEquals(BAD_REQUEST, result.status());
        }

        // @Test
        // public void testGlobalDiversityMissingCategory() {
        // Http.RequestBuilder request = new Http.RequestBuilder()
        // .method(GET)
        // .uri("/diversity//1");

        // Result result = route(app, request);
        // assertEquals(BAD_REQUEST, result.status());
        // }

        // @Test
        // public void testGlobalDiversityMissingId() {
        // Http.RequestBuilder request = new Http.RequestBuilder()
        // .method(GET)
        // .uri("/diversity/movie/");

        // Result result = route(app, request);
        // assertEquals(BAD_REQUEST, result.status());
        // }

        // @Test
        // public void testKnownForEndpointWhenServiceFailsReturnsInternalServerError()
        // {
        // PersonStatsService personStatsService =
        // Mockito.mock(PersonStatsService.class);
        // Mockito.when(personStatsService.getKnownForPage(Mockito.anyInt()))
        // .thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        // TmdbSearchService tmdbSearchService = Mockito.mock(TmdbSearchService.class);
        // FinancialPerformanceService financialPerformanceService = Mockito
        // .mock(FinancialPerformanceService.class);
        // ReadabilityService readabilityService =
        // Mockito.mock(ReadabilityService.class);

        // Application failingApp = new GuiceApplicationBuilder()
        // .configure(Map.of(
        // "tmdb.apiKey", "test-api-key",
        // "tmdb.raToken", "test-ra-token"))
        // .overrides(
        // bind(TmdbSearchService.class).toInstance(tmdbSearchService),
        // bind(FinancialPerformanceService.class)
        // .toInstance(financialPerformanceService),
        // bind(PersonStatsService.class).toInstance(personStatsService),
        // bind(ReadabilityService.class).toInstance(readabilityService))
        // .build();

        // Http.RequestBuilder request = new Http.RequestBuilder()
        // .method(GET)
        // .uri("/person/500/known-for");

        // Result result = route(failingApp, request);
        // assertEquals(INTERNAL_SERVER_ERROR, result.status());
        // }

        @Test
        public void testSearchMovieByIdEndpoint() {
                Http.RequestBuilder request = new Http.RequestBuilder()
                                .method(GET)
                                .uri("/api/movie?id=11");

                Result result = route(app, request);
                assertEquals(OK, result.status());
        }

        /**
         * Tests the /movie/{id} endpoint.
         * Ensures that requesting a movie by ID returns HTTP 200 OK.
         */
        @Test
        public void testMovieDetailsEndpoint() {
                Result result = route(app,
                                fakeRequest(GET, "/movie/11"));
                assertEquals(OK, result.status());
        }

        /**
         * Tests the /tv/{id} endpoint.
         * Ensures that requesting a TV show by ID returns HTTP 200 OK.
         */
        @Test
        public void testTvShowDetailsEndpoint() {
                Result result = route(app,
                                fakeRequest(GET, "/tv/10"));
                assertEquals(OK, result.status());
        }

}
