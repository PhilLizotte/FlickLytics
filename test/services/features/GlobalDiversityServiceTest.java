package services.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.features.diversity.GlobalDiversityService;
import services.tmdb.TmdbConfig;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GlobalDiversityService}.
 *
 * <p>Uses mocked WSClient/WSRequest/WSResponse so tests do NOT call the live TMDb API.</p>
 *
 * @author Chama
 */
public class GlobalDiversityServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void compute_movie_returnsExpectedMetrics() throws Exception {
        // Mock TMDb config
        TmdbConfig tmdbConfig = mock(TmdbConfig.class);
        when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(tmdbConfig.getApiKey()).thenReturn("dummy");

        // Mock WS
        WSClient ws = mock(WSClient.class);
        WSRequest reqLanguages = mock(WSRequest.class);
        WSRequest reqTranslations = mock(WSRequest.class);
        WSResponse respLanguages = mock(WSResponse.class);
        WSResponse respTranslations = mock(WSResponse.class);

        // URL routing
        when(ws.url(contains("/configuration/languages"))).thenReturn(reqLanguages);
        when(ws.url(contains("/movie/"))).thenReturn(reqTranslations);

        // languages endpoint returns array size 4
        JsonNode languagesJson = MAPPER.readTree("[{\"a\":1},{\"a\":2},{\"a\":3},{\"a\":4}]");
        when(respLanguages.asJson()).thenReturn(languagesJson);
        when(reqLanguages.get()).thenReturn(CompletableFuture.completedFuture(respLanguages));

        // translations endpoint: 3 translations total, 2 usable overviews, 1 empty overview
        String translationsPayload = """
        {
          "translations": [
            { "data": { "overview": "Short overview." } },
            { "data": { "overview": "This is a longer overview text." } },
            { "data": { "overview": "" } }
          ]
        }
        """;
        JsonNode translationsJson = MAPPER.readTree(translationsPayload);
        when(respTranslations.asJson()).thenReturn(translationsJson);
        when(reqTranslations.get()).thenReturn(CompletableFuture.completedFuture(respTranslations));

        GlobalDiversityService service = new GlobalDiversityService(ws, tmdbConfig);

        var stats = service.compute("movie", 123).toCompletableFuture().get();

        assertEquals("movie", stats.category);
        assertEquals(123, stats.id);

        assertEquals(3, stats.translationCount);
        assertEquals(4, stats.supportedLanguageCount);
        assertEquals(0.75, stats.translationDensity, 1e-9);

        assertTrue(stats.localizationIndex > 0.0);
        assertTrue(stats.localizationIndex <= 1.0);

        verify(ws, atLeastOnce()).url(anyString());
    }


    @Test
    public void compute_tv_returnsExpectedMetrics() throws Exception {
        // Mock TMDb config
        TmdbConfig tmdbConfig = mock(TmdbConfig.class);
        when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(tmdbConfig.getApiKey()).thenReturn("dummy");

        // Mock WS
        WSClient ws = mock(WSClient.class);
        WSRequest reqLanguages = mock(WSRequest.class);
        WSRequest reqTranslations = mock(WSRequest.class);
        WSResponse respLanguages = mock(WSResponse.class);
        WSResponse respTranslations = mock(WSResponse.class);

        int id = 456;

        // tv path
        when(ws.url(contains("/configuration/languages"))).thenReturn(reqLanguages);
        when(ws.url(contains("/tv/" + id + "/translations"))).thenReturn(reqTranslations);

        // languages
        JsonNode languagesJson = MAPPER.readTree("[{\"a\":1},{\"a\":2}]");
        when(respLanguages.asJson()).thenReturn(languagesJson);
        when(reqLanguages.get()).thenReturn(CompletableFuture.completedFuture(respLanguages));

        // translations
        String translationsPayload = """
    {
      "translations": [
        { "data": { "overview": "TV overview one." } },
        { "data": { "overview": "Another TV overview." } }
      ]
    }
    """;
        JsonNode translationsJson = MAPPER.readTree(translationsPayload);
        when(respTranslations.asJson()).thenReturn(translationsJson);
        when(reqTranslations.get()).thenReturn(CompletableFuture.completedFuture(respTranslations));

        GlobalDiversityService service = new GlobalDiversityService(ws, tmdbConfig);

        var stats = service.compute("tv", id).toCompletableFuture().get();

        // assert
        assertEquals("tv", stats.category);
        assertEquals(id, stats.id);

        assertEquals(2, stats.translationCount);
        assertEquals(2, stats.supportedLanguageCount);
        assertEquals(1.0, stats.translationDensity, 1e-9);

        assertTrue(stats.localizationIndex > 0.0);

        verify(ws).url(contains("/tv/" + id + "/translations"));
    }

    @Test
    public void compute_invalidCategory_returnsZeros_butStillFetchesSupportedLanguages() throws Exception {
        // NOTE: compute() always calls fetchSupportedLanguageCount(), even if category is invalid.
        TmdbConfig tmdbConfig = mock(TmdbConfig.class);
        when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(tmdbConfig.getApiKey()).thenReturn("dummy");

        WSClient ws = mock(WSClient.class);
        WSRequest reqLanguages = mock(WSRequest.class);
        WSResponse respLanguages = mock(WSResponse.class);

        when(ws.url(contains("/configuration/languages"))).thenReturn(reqLanguages);

        JsonNode languagesJson = MAPPER.readTree("[{\"a\":1},{\"a\":2}]"); // supportedLanguageCount = 2
        when(respLanguages.asJson()).thenReturn(languagesJson);
        when(reqLanguages.get()).thenReturn(CompletableFuture.completedFuture(respLanguages));

        GlobalDiversityService service = new GlobalDiversityService(ws, tmdbConfig);

        var stats = service.compute("person", 1).toCompletableFuture().get();

        assertEquals("person", stats.category);
        assertEquals(1, stats.id);
        assertEquals(0, stats.translationCount);
        assertEquals(2, stats.supportedLanguageCount);
        assertEquals(0.0, stats.translationDensity, 1e-9);
        assertEquals(0.0, stats.localizationIndex, 1e-9);

        // Should NOT call translations endpoint for invalid category
        verify(ws, never()).url(contains("/translations"));
        verify(ws, atLeastOnce()).url(contains("/configuration/languages"));
    }

    @Test
    public void compute_movie_missingTranslationsField_hitsTranslationsNullBranch() throws Exception {
        TmdbConfig tmdbConfig = mock(TmdbConfig.class);
        when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(tmdbConfig.getApiKey()).thenReturn("dummy");

        WSClient ws = mock(WSClient.class);
        WSRequest reqLanguages = mock(WSRequest.class);
        WSRequest reqTranslations = mock(WSRequest.class);
        WSResponse respLanguages = mock(WSResponse.class);
        WSResponse respTranslations = mock(WSResponse.class);

        when(ws.url(contains("/configuration/languages"))).thenReturn(reqLanguages);
        when(ws.url(contains("/movie/"))).thenReturn(reqTranslations);

        JsonNode languagesJson = MAPPER.readTree("[{\"a\":1},{\"a\":2},{\"a\":3}]"); // 3
        when(respLanguages.asJson()).thenReturn(languagesJson);
        when(reqLanguages.get()).thenReturn(CompletableFuture.completedFuture(respLanguages));

        // No "translations" field at all => translations == null branch
        JsonNode translationsJson = MAPPER.readTree("{\"somethingElse\":123}");
        when(respTranslations.asJson()).thenReturn(translationsJson);
        when(reqTranslations.get()).thenReturn(CompletableFuture.completedFuture(respTranslations));

        GlobalDiversityService service = new GlobalDiversityService(ws, tmdbConfig);

        var stats = service.compute("movie", 11).toCompletableFuture().get();

        assertEquals(0, stats.translationCount);
        assertEquals(3, stats.supportedLanguageCount);
        assertEquals(0.0, stats.translationDensity, 1e-9);
        assertEquals(0.0, stats.localizationIndex, 1e-9);
    }

    @Test
    public void compute_movie_overviewMissingOrEmpty_hitsSafeTextNull_and_BaseLenZero() throws Exception {
        TmdbConfig tmdbConfig = mock(TmdbConfig.class);
        when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(tmdbConfig.getApiKey()).thenReturn("dummy");

        WSClient ws = mock(WSClient.class);
        WSRequest reqLanguages = mock(WSRequest.class);
        WSRequest reqTranslations = mock(WSRequest.class);
        WSResponse respLanguages = mock(WSResponse.class);
        WSResponse respTranslations = mock(WSResponse.class);

        when(ws.url(contains("/configuration/languages"))).thenReturn(reqLanguages);
        when(ws.url(contains("/movie/"))).thenReturn(reqTranslations);

        JsonNode languagesJson = MAPPER.readTree("[{\"a\":1},{\"a\":2},{\"a\":3},{\"a\":4},{\"a\":5}]"); // 5
        when(respLanguages.asJson()).thenReturn(languagesJson);
        when(reqLanguages.get()).thenReturn(CompletableFuture.completedFuture(respLanguages));

        // translations array exists, but overviews are missing/blank
        String payload = """
        {
          "translations": [
            { "data": { } },
            { "data": { "overview": "   " } }
          ]
        }
        """;
        JsonNode translationsJson = MAPPER.readTree(payload);
        when(respTranslations.asJson()).thenReturn(translationsJson);
        when(reqTranslations.get()).thenReturn(CompletableFuture.completedFuture(respTranslations));

        GlobalDiversityService service = new GlobalDiversityService(ws, tmdbConfig);

        var stats = service.compute("movie", 99).toCompletableFuture().get();

        // translationCount counts array size (2)
        assertEquals(2, stats.translationCount);
        assertEquals(5, stats.supportedLanguageCount);
        assertEquals((double) 2 / 5, stats.translationDensity, 1e-9);

        // baseline stays empty => baseLen==0 => localizationIndex 0.0
        assertEquals(0.0, stats.localizationIndex, 1e-9);
    }

    @Test
    public void compute_movie_twoValidOverviews_hitsBaselineUpdate_and_AverageComputation() throws Exception {
        TmdbConfig tmdbConfig = mock(TmdbConfig.class);
        when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(tmdbConfig.getApiKey()).thenReturn("dummy");

        WSClient ws = mock(WSClient.class);
        WSRequest reqLanguages = mock(WSRequest.class);
        WSRequest reqTranslations = mock(WSRequest.class);
        WSResponse respLanguages = mock(WSResponse.class);
        WSResponse respTranslations = mock(WSResponse.class);

        when(ws.url(contains("/configuration/languages"))).thenReturn(reqLanguages);
        when(ws.url(contains("/movie/"))).thenReturn(reqTranslations);

        JsonNode languagesJson = MAPPER.readTree("[{\"a\":1},{\"a\":2},{\"a\":3},{\"a\":4}]"); // 4
        when(respLanguages.asJson()).thenReturn(languagesJson);

        when(reqLanguages.get()).thenReturn(CompletableFuture.completedFuture(respLanguages));

        // longest is length 10, second is length 5
        String payload = """
        {
          "translations": [
            { "data": { "overview": "1234567890" } },
            { "data": { "overview": "12345" } }
          ]
        }
        """;
        JsonNode translationsJson = MAPPER.readTree(payload);
        when(respTranslations.asJson()).thenReturn(translationsJson);
        when(reqTranslations.get()).thenReturn(CompletableFuture.completedFuture(respTranslations));

        GlobalDiversityService service = new GlobalDiversityService(ws, tmdbConfig);

        var stats = service.compute("movie", 268).toCompletableFuture().get();

        assertEquals(2, stats.translationCount);
        assertEquals(4, stats.supportedLanguageCount);
        assertEquals(0.5, stats.translationDensity, 1e-9);

        // avg(10/10, 5/10) = 0.75
        assertEquals(0.75, stats.localizationIndex, 1e-9);
    }


    private Object invokePrivate(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = GlobalDiversityService.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    @Test
    public void safeText_shouldReturnEmpty_whenNodeIsNull() throws Exception {
        String result = (String) invokePrivate("safeText", new Class[]{JsonNode.class}, (Object) null);
        assertEquals("", result);
    }

    @Test
    public void safeText_shouldReturnEmpty_whenNodeIsJsonNull() throws Exception {
        JsonNode nullNode = NullNode.getInstance();

        String result = (String) invokePrivate("safeText", new Class[]{JsonNode.class}, nullNode);
        assertEquals("", result);
    }

    @Test
    public void safeText_shouldReturnText_whenNodeHasValue() throws Exception {
        JsonNode textNode = new TextNode("hello");

        String result = (String) invokePrivate("safeText", new Class[]{JsonNode.class}, textNode);
        assertEquals("hello", result);
    }

    @Test
    public void encode_shouldHandleNull() throws Exception {
        String result = (String) invokePrivate("encode", new Class[]{String.class}, (Object) null);
        assertEquals("", result); // encode("") == ""
    }

    @Test
    public void encode_shouldEncodeNormalString() throws Exception {
        String input = "hello world";

        String result = (String) invokePrivate("encode", new Class[]{String.class}, input);
        assertEquals(URLEncoder.encode(input, StandardCharsets.UTF_8), result);
    }

    @Test
    public void encode_shouldEncodeSpecialCharacters() throws Exception {
        String input = "a+b@c";

        String result = (String) invokePrivate("encode", new Class[]{String.class}, input);
        assertEquals(URLEncoder.encode(input, StandardCharsets.UTF_8), result);
    }


    private WSClient ws;
    private TmdbConfig config;
    private GlobalDiversityService service;

    @Before
    public void setup() {
        ws = mock(WSClient.class);
        config = mock(TmdbConfig.class);

        service = new GlobalDiversityService(ws, config);
    }

    private Object invoke(String name, Class<?>[] types, Object... args) throws Exception {
        Method m = GlobalDiversityService.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(service, args);
    }

    @Test
    public void shouldReturnZero_whenBaselineIsNull() throws Exception {
        double result = (double) invoke(
                "computeLocalizationIndex",
                new Class[]{String.class, List.class},
                null,
                Arrays.asList("hello", "world")
        );

        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void shouldReturnZero_whenBaselineIsBlank() throws Exception {
        double result = (double) invoke(
                "computeLocalizationIndex",
                new Class[]{String.class, List.class},
                "   ",
                Arrays.asList("hello")
        );

        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void shouldReturnZero_whenAllTranslationsInvalid() throws Exception {
        double result = (double) invoke(
                "computeLocalizationIndex",
                new Class[]{String.class, List.class},
                "hello",
                Arrays.asList(null, "   ")
        );

        assertEquals(0.0, result, 1e-6);
    }

    @Test
    public void shouldComputeAverageRatio_correctly() throws Exception {
        double result = (double) invoke(
                "computeLocalizationIndex",
                new Class[]{String.class, List.class},
                "hello", // length = 5
                Arrays.asList("hi", "world!") // lengths: 2 , 6
        );

        double expected = ((2.0/5.0) + (6.0/5.0)) / 2.0;
        assertEquals(expected, result, 1e-6);
    }

    @Test
    public void shouldIgnoreNullAndBlankTranslations() throws Exception {
        double result = (double) invoke(
                "computeLocalizationIndex",
                new Class[]{String.class, List.class},
                "abcd",
                Arrays.asList(null, "  ", "ab")
        );

        assertEquals(0.5, result, 1e-6);
    }

    @Test
    public void shouldReturnZero_whenListIsEmpty() throws Exception {
        double result = (double) invoke(
                "computeLocalizationIndex",
                new Class[]{String.class, List.class},
                "hello",
                Collections.emptyList()
        );

        assertEquals(0.0, result, 1e-6);
    }

}