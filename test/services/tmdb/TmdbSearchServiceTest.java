package services.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

// Sorry for putting review stuff in here, but it makes it a lot easier to get 100% coverage.
// tldr I had to move the tmdb query from the review service into the tmdb service,
// and due to my structure, it's a lot easier to test a bunch of things at once
// to get full coverage 
import models.domain.Review;
import services.features.reviews.ReviewSentimentService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import play.libs.ws.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void testSearchTVShowsEnrichesGenresAndCachesGenreList() {
        String baseUrl = "https://api.example";

        WSClient ws = mock(WSClient.class);
        TmdbConfig config = mock(TmdbConfig.class);
        when(config.getBaseUrl()).thenReturn(baseUrl);
        when(config.getApiKey()).thenReturn("api_key");

        WSRequest genreReq = mock(WSRequest.class);
        when(genreReq.addQueryParameter(anyString(), anyString())).thenReturn(genreReq);
        WSResponse genreResp = mock(WSResponse.class);
        when(genreResp.asJson()).thenReturn(Json.parse("{\"genres\":[{\"id\":16,\"name\":\"Animation\"}]}"));
        when(genreReq.get()).thenReturn(CompletableFuture.completedFuture(genreResp));

        WSRequest searchReq = mock(WSRequest.class);
        when(searchReq.addQueryParameter(anyString(), anyString())).thenReturn(searchReq);
        WSResponse searchResp = mock(WSResponse.class);
        when(searchResp.asJson()).thenReturn(Json.parse(
                "{\"results\":[{\"id\":5,\"genre_ids\":[16],\"first_air_date\":\"2020-01-02\",\"original_language\":\"en\"}]}"));
        when(searchReq.get()).thenReturn(CompletableFuture.completedFuture(searchResp));

        // ReviewSentimentService uses the same WSClient, so we must provide requests
        // for the reviews URL.
        WSRequest reviewsReq = mock(WSRequest.class);
        when(reviewsReq.addQueryParameter(anyString(), anyString())).thenReturn(reviewsReq);
        WSResponse reviewsResp = mock(WSResponse.class);
        when(reviewsResp.asJson()).thenReturn(Json.parse("{\"results\":[]}"));
        when(reviewsReq.get()).thenReturn(CompletableFuture.completedFuture(reviewsResp));

        when(ws.url(baseUrl + "/genre/tv/list")).thenReturn(genreReq);
        when(ws.url(baseUrl + "/search/tv")).thenReturn(searchReq);
        when(ws.url(baseUrl + "/tv/5/reviews")).thenReturn(reviewsReq);

        TmdbSearchService service = new TmdbSearchService(ws, config);

        JsonNode enriched1 = service.search("tv", "batman").toCompletableFuture().join();
        JsonNode item1 = enriched1.get("results").get(0);

        assertEquals("https://www.themoviedb.org/tv/5", item1.get("detailsUrl").asText());
        assertEquals("2020-01-02", item1.get("first_air_date").asText());
        assertEquals("en", item1.get("original_language").asText());
        assertTrue(item1.get("genres").isArray());
        assertEquals("Animation", item1.get("genres").get(0).asText());

        // second call should reuse cached genre map
        JsonNode enriched2 = service.search("tv", "batman").toCompletableFuture().join();
        assertNotNull(enriched2);

        verify(ws, times(1)).url(baseUrl + "/genre/tv/list");
    }

    @Test
    public void shouldAddEmptyGenres_whenGenreIdsMissing() throws Exception {
        ObjectNode node = Json.newObject();
        Map<Integer, String> genreMap = new HashMap<>();

        TmdbSearchService service = new TmdbSearchService(null, null);

        Field field = TmdbSearchService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, new ObjectMapper());

        Method method = TmdbSearchService.class.getDeclaredMethod("addGenreNames", ObjectNode.class, Map.class);
        method.setAccessible(true);

        method.invoke(service, node, genreMap);

        assertEquals(0, node.get("genres").size());
    }

    @Test
    public void shouldAddEmptyGenres_whenGenreIdsNotArray() throws Exception {
        ObjectNode node = Json.newObject();
        node.put("genre_ids", 123);

        Map<Integer, String> genreMap = new HashMap<>();

        TmdbSearchService service = new TmdbSearchService(null, null);

        Field field = TmdbSearchService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, new ObjectMapper());

        Method method = TmdbSearchService.class.getDeclaredMethod("addGenreNames", ObjectNode.class, Map.class);
        method.setAccessible(true);

        method.invoke(service, node, genreMap);

        assertEquals(0, node.get("genres").size());
    }

    @Test
    public void shouldAddOnlyValidGenres() throws Exception {
        ObjectNode node = Json.newObject();
        ArrayNode ids = Json.newArray();
        ids.add(1);
        ids.add(2);
        ids.add(999);
        node.set("genre_ids", ids);

        Map<Integer, String> genreMap = new HashMap<>();
        genreMap.put(1, "Action");
        genreMap.put(2, "Comedy");

        TmdbSearchService service = new TmdbSearchService(null, null);

        Field field = TmdbSearchService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, new ObjectMapper());

        Method method = TmdbSearchService.class.getDeclaredMethod("addGenreNames", ObjectNode.class, Map.class);
        method.setAccessible(true);

        method.invoke(service, node, genreMap);

        ArrayNode result = (ArrayNode) node.get("genres");

        assertEquals(2, result.size());
        assertEquals("Action", result.get(0).asText());
        assertEquals("Comedy", result.get(1).asText());
    }

    @Test
    public void shouldIgnoreNonIntValues() throws Exception {
        ObjectNode node = Json.newObject();
        ArrayNode ids = Json.newArray();
        ids.add("abc");
        ids.add(1);
        node.set("genre_ids", ids);

        Map<Integer, String> genreMap = new HashMap<>();
        genreMap.put(1, "Action");

        TmdbSearchService service = new TmdbSearchService(null, null);

        // inject objectMapper
        Field field = TmdbSearchService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, new ObjectMapper());

        Method method = TmdbSearchService.class.getDeclaredMethod("addGenreNames", ObjectNode.class, Map.class);
        method.setAccessible(true);

        method.invoke(service, node, genreMap);

        ArrayNode result = (ArrayNode) node.get("genres");

        assertEquals(1, result.size());
        assertEquals("Action", result.get(0).asText());
    }

    @Test
    public void shouldReturnEmpty_whenProfilePathIsMissing() throws Exception {
        ObjectNode node = Json.newObject();

        TmdbSearchService service = mock(TmdbSearchService.class);

        Method method = TmdbSearchService.class.getDeclaredMethod("profileUrl", ObjectNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, node);

        assertEquals("", result);
    }

    @Test
    public void shouldReturnEmpty_whenProfilePathIsNull() throws Exception {
        ObjectNode node = Json.newObject();
        node.set("profile_path", NullNode.instance);

        TmdbSearchService service = mock(TmdbSearchService.class);

        Method method = TmdbSearchService.class.getDeclaredMethod("profileUrl", ObjectNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, node);

        assertEquals("", result);
    }

    @Test
    public void shouldReturnEmpty_whenProfilePathIsNotText() throws Exception {
        ObjectNode node = Json.newObject();
        node.put("profile_path", 123);

        TmdbSearchService service = mock(TmdbSearchService.class);

        Method method = TmdbSearchService.class.getDeclaredMethod("profileUrl", ObjectNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, node);

        assertEquals("", result);
    }

    @Test
    public void shouldReturnUrl_whenProfilePathIsValid() throws Exception {
        ObjectNode node = Json.newObject();
        node.put("profile_path", "/abc.jpg");

        TmdbSearchService service = mock(TmdbSearchService.class);

        Method method = TmdbSearchService.class.getDeclaredMethod("profileUrl", ObjectNode.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, node);

        assertEquals("https://image.tmdb.org/t/p/original/abc.jpg", result);
    }

    @Test
    public void shouldSetFields_whenMovieAndFieldsExist() throws Exception {
        ObjectNode node = Json.newObject();
        node.put("release_date", "2020-01-01");
        node.put("original_language", "en");

        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "normalizeCommonMovieTvFields", ObjectNode.class, boolean.class);
        method.setAccessible(true);

        method.invoke(service, node, true);

        assertEquals("2020-01-01", node.get("releaseDate").asText());
        assertEquals("en", node.get("language").asText());
    }

    @Test
    public void shouldNotSetFields_whenMovieAndFieldsMissing() throws Exception {
        ObjectNode node = Json.newObject();

        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "normalizeCommonMovieTvFields", ObjectNode.class, boolean.class);
        method.setAccessible(true);

        method.invoke(service, node, true);

        assertEquals(null, node.get("releaseDate"));
        assertEquals(null, node.get("language"));
    }

    @Test
    public void shouldSetFields_whenTvAndFieldsExist() throws Exception {
        ObjectNode node = Json.newObject();
        node.put("first_air_date", "2019-05-05");
        node.put("original_language", "fr");

        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "normalizeCommonMovieTvFields", ObjectNode.class, boolean.class);
        method.setAccessible(true);

        method.invoke(service, node, false);

        assertEquals("2019-05-05", node.get("releaseDate").asText());
        assertEquals("fr", node.get("language").asText());
    }

    @Test
    public void shouldNotSetFields_whenTvAndFieldsMissing() throws Exception {
        ObjectNode node = Json.newObject();

        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "normalizeCommonMovieTvFields", ObjectNode.class, boolean.class);
        method.setAccessible(true);

        method.invoke(service, node, false);

        assertEquals(null, node.get("releaseDate"));
        assertEquals(null, node.get("language"));
    }

    //
    @Test
    public void shouldUseMinusOne_whenIdMissing() throws Exception {
        ObjectNode root = Json.newObject();
        ArrayNode arr = Json.newArray();

        ObjectNode item = Json.newObject(); // no id
        arr.add(item);

        root.set("results", arr);

        TmdbSearchService service = new TmdbSearchService(null, null);

        Field field = TmdbSearchService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, new ObjectMapper());

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "enrichResults", String.class, JsonNode.class, Map.class);
        method.setAccessible(true);

        JsonNode result = (JsonNode) method.invoke(service, "movie", root, new HashMap<>());

        String url = result.get("results").get(0).get("detailsUrl").asText();

        assertEquals(true, url.contains("-1"));
    }

    @Test
    public void shouldSkipInvalidItems() throws Exception {
        ObjectNode root = Json.newObject();
        ArrayNode arr = Json.newArray();

        arr.addNull(); // null
        arr.add(123); // not object

        root.set("results", arr);

        TmdbSearchService service = new TmdbSearchService(null, null);

        Field field = TmdbSearchService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, new ObjectMapper());

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "enrichResults", String.class, JsonNode.class, Map.class);
        method.setAccessible(true);

        JsonNode result = (JsonNode) method.invoke(service, "movie", root, new HashMap<>());

        assertEquals(0, result.get("results").size());
    }

    @Test
    public void shouldReturnRoot_whenResultsNotArray() throws Exception {
        ObjectNode node = Json.newObject();
        node.put("results", 123);

        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "enrichResults", String.class, JsonNode.class, Map.class);
        method.setAccessible(true);

        JsonNode result = (JsonNode) method.invoke(service, "movie", node, new HashMap<>());

        assertEquals(true, result.get("results").isInt());
    }

    @Test
    public void shouldReturnRoot_whenResultsMissing() throws Exception {
        ObjectNode node = Json.newObject();

        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "enrichResults", String.class, JsonNode.class, Map.class);
        method.setAccessible(true);

        JsonNode result = (JsonNode) method.invoke(service, "movie", node, new HashMap<>());

        assertEquals(true, result.has("results") == false);
    }

    @Test
    public void shouldReturnSame_whenSearchJsonNotObject() throws Exception {
        JsonNode node = Json.newArray(); // 👈 not object

        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "enrichResults", String.class, JsonNode.class, Map.class);
        method.setAccessible(true);

        JsonNode result = (JsonNode) method.invoke(service, "movie", node, new HashMap<>());

        assertEquals(node, result);
    }

    @Test
    public void shouldReturnNull_whenSearchJsonIsNull() throws Exception {
        TmdbSearchService service = new TmdbSearchService(null, null);

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "enrichResults", String.class, JsonNode.class, Map.class);
        method.setAccessible(true);

        JsonNode result = (JsonNode) method.invoke(service, "movie", null, new HashMap<>());

        assertEquals(null, result);
    }

    @Test
    public void shouldSkipPersonBranch_whenCategoryIsSomethingElse() throws Exception {
        ObjectNode root = Json.newObject();
        ArrayNode arr = Json.newArray();

        ObjectNode item = Json.newObject();
        item.put("id", 5);

        arr.add(item);
        root.set("results", arr);

        TmdbSearchService service = new TmdbSearchService(null, null);

        Field field = TmdbSearchService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, new ObjectMapper());

        Method method = TmdbSearchService.class.getDeclaredMethod(
                "enrichResults", String.class, JsonNode.class, Map.class);
        method.setAccessible(true);

        JsonNode result = (JsonNode) method.invoke(service, "unknown", root, new HashMap<>());

        JsonNode res = result.get("results").get(0);

        assertEquals(false, res.has("photoUrl"));
        assertEquals(false, res.has("knownForUrl"));
    }

    @Mock
    WSClient ws;
    @Mock
    WSRequest wsRequest;
    @Mock
    WSResponse wsResponse;
    @Mock
    TmdbConfig tmdbConfig;
    @InjectMocks
    TmdbSearchService service;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        Field cacheField = TmdbSearchService.class.getDeclaredField("genreCache");
        cacheField.setAccessible(true);
        Map<String, Object> genreCache = (Map<String, Object>) cacheField.get(service);
        genreCache.clear();
    }

    @Test
    public void testCacheNullTriggersHttpCall() throws Exception {
        // Mock HTTP
        when(tmdbConfig.getBaseUrl()).thenReturn("http://fakeurl");
        when(tmdbConfig.getApiKey()).thenReturn("fakeKey");
        when(ws.url(anyString())).thenReturn(wsRequest);
        when(wsRequest.addQueryParameter(anyString(), anyString())).thenReturn(wsRequest);
        when(wsRequest.get()).thenReturn(CompletableFuture.completedFuture(wsResponse));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree("{\"genres\":[{\"id\":1,\"name\":\"Action\"}]}");
        when(wsResponse.asJson()).thenReturn(json);

        Method fetchMethod = TmdbSearchService.class.getDeclaredMethod("fetchGenreMap", String.class);
        fetchMethod.setAccessible(true);
        CompletionStage<Map<Integer, String>> resultStage = (CompletionStage<Map<Integer, String>>) fetchMethod
                .invoke(service, "movie");

        Map<Integer, String> result = resultStage.toCompletableFuture().get();

        assertEquals(1, result.size());
        assertEquals("Action", result.get(1));

        Field cacheField = TmdbSearchService.class.getDeclaredField("genreCache");
        cacheField.setAccessible(true);
        Map<String, Object> genreCache = (Map<String, Object>) cacheField.get(service);
        assertTrue(genreCache.containsKey("movie"));
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test the API call method of the ReviewSentimentService. API calls are
     *         all
     *         mocked.
     * 
     * @throws Exception Can throw some exception related to getting the completed
     *                   future.
     */
    @Test
    public void fetchReviewsReviewTest() throws Exception {
        WSClient ws = mock(WSClient.class);
        TmdbConfig config = mock(TmdbConfig.class);
        when(config.getBaseUrl()).thenReturn("https://api.example");
        when(config.getApiKey()).thenReturn("api_key");

        WSRequest request = mock(WSRequest.class);
        WSResponse response = mock(WSResponse.class);

        TmdbSearchService service = new TmdbSearchService(ws, config);

        ObjectMapper mapper = new ObjectMapper();

        JsonNode apiJson = mapper.readTree(
                """
                        {
                          "id": -1,
                          "page": 1,
                          "results": [
                            {
                                "author": "Bob Angelson",
                                "author_details": {
                                    "name": "bob",
                                    "username": "angel",
                                    "avatar_path": null,
                                    "rating": null
                                },
                                "content": "I love factorio so much!",
                                "created_at": "2012-06-05T23:00:24.000Z",
                                "id": "abc123",
                                "updated_at": "2012-06-05T23:00:24.000Z",
                                "url": "www.nah_I_aint_making_urls.ca"
                            },
                            {
                                "author": "Mr. Wizard",
                                "author_details": {
                                    "name": "The Almighty",
                                    "username": "Secretely_the_devil",
                                    "avatar_path": null,
                                    "rating": null
                                },
                                "content": "One day I shall rule the world, and you will all know my awesome power!",
                                "created_at": "2012-06-05T23:00:24.000Z",
                                "id": "abc456",
                                "updated_at": "2012-06-05T23:00:24.000Z",
                                "url": "www.nah_I_aint_making_urls.ca"
                            },
                            {
                                "author": "Death",
                                "author_details": {
                                    "name": "Thanatos",
                                    "username": "reaper_man22",
                                    "avatar_path": null,
                                    "rating": null
                                },
                                "content": "It was a good movie to watch while going on my daily reaping rounds! I highly recommend it to everyone!",
                                "created_at": "2012-06-05T23:00:24.000Z",
                                "id": "abc789",
                                "updated_at": "2012-06-05T23:00:24.000Z",
                                "url": "www.nah_I_aint_making_urls.ca"
                            },
                            {
                                "author": "Sheep",
                                "author_details": {
                                    "name": "bahh",
                                    "username": "BAHHHHHH",
                                    "avatar_path": null,
                                    "rating": null
                                },
                                "content": "bah bah BAhh baaAAAhh bbbbAAAAHHHHHHHHHH",
                                "created_at": "2012-06-05T23:00:24.000Z",
                                "id": "def123",
                                "updated_at": "2012-06-05T23:00:24.000Z",
                                "url": "www.nah_I_aint_making_urls.ca"
                            },
                            {
                                "author": "Bowser",
                                "author_details": {
                                    "name": "The Koopa King",
                                    "username": "Totally the real Mario",
                                    "avatar_path": null,
                                    "rating": null
                                },
                                "content": "BWAHAHA! I captured princess peach once again! Now she'll love me for sure! Oh wait, I mean, itsa me, Mario! Princess, I ave come to save you! I love you sooo much!",
                                "created_at": "2012-06-05T23:00:24.000Z",
                                "id": "def456",
                                "updated_at": "2012-06-05T23:00:24.000Z",
                                "url": "www.nah_I_aint_making_urls.ca"
                            },
                            {
                                "author": "The Big Cheese",
                                "author_details": {
                                    "name": "Boss man",
                                    "username": "Boss mans number one helper",
                                    "avatar_path": null,
                                    "rating": null
                                },
                                "content": "So, the boss man told me to write a review for this thing. I dont know what to write. like, I'm supposed to know what boss man is thinking? How am I supposed to know that? I didn't even watch the damn thing. Sigh. Im overworked, boss man is terrible at keeping track of his schedule, and on top of that I need to do all of these annoying tasks for him. Has he even considered the poor workers that he's forcing to do all these dreaful tasks! It's terrible. I want to leave this disaster of a job behind me already. Anyway, uhh, I think I've hit my word limit now, so that should be dealt with.",
                                "created_at": "2012-06-05T23:00:24.000Z",
                                "id": "def789",
                                "updated_at": "2012-06-05T23:00:24.000Z",
                                "url": "www.nah_I_aint_making_urls.ca"
                            }
                          ]
                        }
                        """);

        when(config.getBaseUrl()).thenReturn("https://api.test.com");
        when(config.getApiKey()).thenReturn("fakeAPIKey");

        when(response.asJson()).thenReturn(apiJson);

        when(ws.url(anyString())).thenReturn(request);
        when(request.addQueryParameter(anyString(), anyString())).thenReturn(request);
        when(request.get()).thenReturn(
                CompletableFuture.completedFuture(response));

        Review review = new ReviewSentimentService()
                .extractSentiment(service.fetchReviewsAsRawList("", -1).toCompletableFuture().get());

        // review = reviewStage.toCompletableFuture().get();

        String[] expected = { ":-)", ":|", ":-)", ":|", ":-)", ":-(" };

        // check all the reviews
        assertEquals("fetch review 0", review.getSentimentAtIndex(0), expected[0]);
        assertEquals("fetch review 1", review.getSentimentAtIndex(1), expected[1]);
        assertEquals("fetch review 2", review.getSentimentAtIndex(2), expected[2]);
        assertEquals("fetch review 3", review.getSentimentAtIndex(3), expected[3]);
        assertEquals("fetch review 4", review.getSentimentAtIndex(4), expected[4]);
        assertEquals("fetch review 5", review.getSentimentAtIndex(5), expected[5]);

        assertEquals("fetch overall", review.getOverallSentiment(), ":|");

        assertEquals("fetch review preservation", review.getReviews().get(0),
                "I love factorio so much!");
    }
}
