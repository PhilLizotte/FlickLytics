package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.Application;
import play.api.test.Helpers;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import services.features.financial.FinancialPerformanceService;
import services.features.personstats.PersonStatsService;
import services.features.readability.ReadabilityService;
import services.tmdb.TmdbSearchService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/** A page that tests all methods in HomeController.java.</br>
 *  Due to the MVC architecture and the presence of only one controller,
 *  this should test all logical functionality in the application.
 */
public class HomeControllerTest extends WithApplication {

    private static TmdbSearchController controller;
    private static TmdbSearchService tmdbSearchService;
    private static PersonStatsService personStatsService;
    private static ReadabilityService readabilityService;
    private static FinancialPerformanceService fpService;

    private JsonNode resultJson(Result result) throws Exception {
        return new ObjectMapper().readTree(contentAsString(result));
    }

    @Before
    public void setup() {
        tmdbSearchService = mock(TmdbSearchService.class);
        personStatsService = mock(PersonStatsService.class);
        readabilityService = mock(ReadabilityService.class);
        fpService = mock(FinancialPerformanceService.class);
        controller = new TmdbSearchController(
                tmdbSearchService, personStatsService, readabilityService, fpService
        );
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
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
    public void testKnownFor() {
        // Valid request
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/person/18918/known-for");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }
    
    @Test
    public void testFinances() {
        // Valid request
        Http.RequestBuilder request = new Http.RequestBuilder()
            .method(GET)
            .uri("/finances/11");

        Result result = route(app, request);
        assertEquals(OK, result.status());
        
        // Invalid id
        request = new Http.RequestBuilder()
                .method(GET)
                .uri("/finances/-1");

        result = route(app, request);
        assertNotEquals(OK, result.status());
    }

    @Test
    public void testSearch() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode fakeJson = mapper.readTree("""
            { "results": [ { "title": "TestMovie"} ] }
        """);

        when(tmdbSearchService.search("movie", "Batman"))
                .thenReturn(CompletableFuture.completedFuture(fakeJson));

        // Valid search test
        CompletionStage<Result> resultStage = controller.search("movie", "Batman");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(200, result.status());
        JsonNode body = resultJson(result);
        assertEquals("TestMovie", body.get("results").get(0).get("title").asText());

        // Blank category test
        result = controller.search(null, "Batman")
                .toCompletableFuture()
                .join();

        assertEquals(400, result.status());

        // Blank query test
        result = controller.search("movie", "")
                .toCompletableFuture()
                .join();

        assertEquals(400, result.status());

        // Invalid category test
        when(controller.search(anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(
                        new RuntimeException("API error")
                ));

        result = controller.search("badCategory", "Matrix")
                .toCompletableFuture()
                .join();

        assertEquals(400, result.status());
    }

    @Test
    public void testMovieDetailsEndpoint() {
        // Valid request
        Result result = route(app,
                fakeRequest(GET, "/movie/11")
        );
        assertEquals(OK, result.status());

        // Invalid request
        result = route(app,
                fakeRequest(GET, "/movie/-1")
        );
        assertNotEquals(OK, result.status());
    }

    @Test
    public void testTvShowDetailsEndpoint() {
        // Valid request
        Result result = route(app,
                fakeRequest(GET, "/tv/2316")
        );
        assertEquals(OK, result.status());
        
        // Invalid request
        result = route(app,
                fakeRequest(GET, "/tv/-1")
        );
        assertNotEquals(OK, result.status());
    }

}
