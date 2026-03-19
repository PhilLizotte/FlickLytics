package services.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TmdbSearchService}.
 * <p>
 * Verifies that TmdbSearchService methods correctly construct TMDb URLs,
 * parse responses, and enrich search results.
 * </p>
 *
 * @author Aram Zand
 */
public class TmdbSearchServiceTest {

    @Test
    public void testMovieDetailsMapsDtoToDomainAndUsesCorrectUrl() {
        String baseUrl = "https://api.example";

        WSClient ws = mock(WSClient.class);
        TmdbConfig config = mock(TmdbConfig.class);
        when(config.getBaseUrl()).thenReturn(baseUrl);
        when(config.getApiKey()).thenReturn("api_key");

        WSRequest req = mock(WSRequest.class);
        when(req.addQueryParameter(anyString(), anyString())).thenReturn(req);

        WSResponse resp = mock(WSResponse.class);
        JsonNode movieJson = Json
                .parse("{\"id\":1,\"title\":\"Test\",\"overview\":\"overview\",\"release_date\":\"2020-02-02\"}");
        when(resp.asJson()).thenReturn(movieJson);
        when(req.get()).thenReturn(CompletableFuture.completedFuture(resp));

        when(ws.url(baseUrl + "/movie/1")).thenReturn(req);

        TmdbSearchService service = new TmdbSearchService(ws, config);
        models.domain.Movie result = service.movieDetails(1).toCompletableFuture().join();

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test", result.getName());

        verify(ws, times(1)).url(baseUrl + "/movie/1");
    }

    @Test
    public void testTvDetailsMapsDtoToDomainAndUsesCorrectUrl() {
        String baseUrl = "https://api.example";

        WSClient ws = mock(WSClient.class);
        TmdbConfig config = mock(TmdbConfig.class);
        when(config.getBaseUrl()).thenReturn(baseUrl);
        when(config.getApiKey()).thenReturn("api_key");

        WSRequest req = mock(WSRequest.class);
        when(req.addQueryParameter(anyString(), anyString())).thenReturn(req);

        WSResponse resp = mock(WSResponse.class);
        JsonNode tvJson = Json.parse(
                "{\"id\":2,\"name\":\"Show\",\"overview\":\"overview\",\"first_air_date\":\"2020-01-01\",\"last_air_date\":\"2020-12-31\"}");
        when(resp.asJson()).thenReturn(tvJson);
        when(req.get()).thenReturn(CompletableFuture.completedFuture(resp));

        when(ws.url(baseUrl + "/tv/2")).thenReturn(req);

        TmdbSearchService service = new TmdbSearchService(ws, config);
        models.domain.TVShow result = service.tvDetails(2).toCompletableFuture().join();

        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("Show", result.getName());

        verify(ws, times(1)).url(baseUrl + "/tv/2");
    }

    @Test
    public void testSearchInvalidCategoryFails() {
        WSClient ws = mock(WSClient.class);
        TmdbConfig config = mock(TmdbConfig.class);
        when(config.getBaseUrl()).thenReturn("https://api.example");
        when(config.getApiKey()).thenReturn("api_key");

        TmdbSearchService service = new TmdbSearchService(ws, config);

        CompletionStage<JsonNode> stage = service.search("invalid", "q");
        try {
            stage.toCompletableFuture().join();
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException);
            assertEquals("Invalid category", ex.getCause().getMessage());
            return;
        }

        throw new AssertionError("Expected invalid category search to fail");
    }

    @Test
    public void testSearchPersonEnrichesPhotoAndKnownForUrls() {
        String baseUrl = "https://api.example";

        WSClient ws = mock(WSClient.class);
        TmdbConfig config = mock(TmdbConfig.class);
        when(config.getBaseUrl()).thenReturn(baseUrl);
        when(config.getApiKey()).thenReturn("api_key");

        WSRequest req = mock(WSRequest.class);
        when(req.addQueryParameter(anyString(), anyString())).thenReturn(req);

        WSResponse resp = mock(WSResponse.class);
        JsonNode searchJson = Json.parse("{\"results\":[{\"id\":10,\"profile_path\":\"/p.png\"}]}");
        when(resp.asJson()).thenReturn(searchJson);
        when(req.get()).thenReturn(CompletableFuture.completedFuture(resp));

        when(ws.url(baseUrl + "/search/person")).thenReturn(req);

        TmdbSearchService service = new TmdbSearchService(ws, config);
        JsonNode enriched = service.search("person", "john").toCompletableFuture().join();

        assertNotNull(enriched);
        assertTrue(enriched.get("results").isArray());
        JsonNode item = enriched.get("results").get(0);
        assertEquals("https://image.tmdb.org/t/p/original/p.png", item.get("photoUrl").asText());
        assertEquals("https://www.themoviedb.org/person/10#known_for", item.get("knownForUrl").asText());
    }

    @Test
    public void testSearchMovieEnrichesGenresAndCachesGenreList() {
        String baseUrl = "https://api.example";

        WSClient ws = mock(WSClient.class);
        TmdbConfig config = mock(TmdbConfig.class);
        when(config.getBaseUrl()).thenReturn(baseUrl);
        when(config.getApiKey()).thenReturn("api_key");

        WSRequest genreReq = mock(WSRequest.class);
        when(genreReq.addQueryParameter(anyString(), anyString())).thenReturn(genreReq);
        WSResponse genreResp = mock(WSResponse.class);
        when(genreResp.asJson()).thenReturn(Json.parse("{\"genres\":[{\"id\":1,\"name\":\"Action\"}]}"));
        when(genreReq.get()).thenReturn(CompletableFuture.completedFuture(genreResp));

        WSRequest searchReq = mock(WSRequest.class);
        when(searchReq.addQueryParameter(anyString(), anyString())).thenReturn(searchReq);
        WSResponse searchResp = mock(WSResponse.class);
        when(searchResp.asJson()).thenReturn(Json.parse(
                "{\"results\":[{\"id\":5,\"genre_ids\":[1],\"release_date\":\"2020-01-02\",\"original_language\":\"en\"}]}"));
        when(searchReq.get()).thenReturn(CompletableFuture.completedFuture(searchResp));

        // ReviewSentimentService uses the same WSClient, so we must provide requests
        // for the reviews URL.
        WSRequest reviewsReq = mock(WSRequest.class);
        when(reviewsReq.addQueryParameter(anyString(), anyString())).thenReturn(reviewsReq);
        WSResponse reviewsResp = mock(WSResponse.class);
        when(reviewsResp.asJson()).thenReturn(Json.parse("{\"results\":[]}"));
        when(reviewsReq.get()).thenReturn(CompletableFuture.completedFuture(reviewsResp));

        when(ws.url(baseUrl + "/genre/movie/list")).thenReturn(genreReq);
        when(ws.url(baseUrl + "/search/movie")).thenReturn(searchReq);
        when(ws.url(baseUrl + "/movie/5/reviews")).thenReturn(reviewsReq);

        TmdbSearchService service = new TmdbSearchService(ws, config);

        JsonNode enriched1 = service.search("movie", "batman").toCompletableFuture().join();
        JsonNode item1 = enriched1.get("results").get(0);

        assertEquals("https://www.themoviedb.org/movie/5", item1.get("detailsUrl").asText());
        assertEquals("2020-01-02", item1.get("releaseDate").asText());
        assertEquals("en", item1.get("language").asText());
        assertTrue(item1.get("genres").isArray());
        assertEquals("Action", item1.get("genres").get(0).asText());

        // second call should reuse cached genre map
        JsonNode enriched2 = service.search("movie", "batman").toCompletableFuture().join();
        assertNotNull(enriched2);

        verify(ws, times(1)).url(baseUrl + "/genre/movie/list");
    }
}
