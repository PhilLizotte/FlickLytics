package services.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.features.diversity.GlobalDiversityService;
import services.tmdb.TmdbConfig;
import models.dto.GlobalDiversityStats;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GlobalDiversityService}.
 * <p>
 * Ensures Global Diversity metrics are computed correctly without calling the live TMDb API.
 * Uses mocks for WSClient/WSRequest/WSResponse to simulate TMDb JSON responses.
 * </p>
 *
 * Author: Chama
 */
public class GlobalDiversityServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Tests compute() for a movie with mocked TMDb responses. */
    @Test
    public void testComputeMovieMetrics() throws Exception {
        // --- Mocks
        WSClient ws = mock(WSClient.class);
        TmdbConfig cfg = mock(TmdbConfig.class);

        when(cfg.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(cfg.getApiKey()).thenReturn("dummy");

        WSRequest languagesReq = mock(WSRequest.class);
        WSRequest translationsReq = mock(WSRequest.class);

        when(ws.url(contains("/configuration/languages"))).thenReturn(languagesReq);
        when(ws.url(contains("/movie/27205/translations"))).thenReturn(translationsReq);

        WSResponse languagesResp = mock(WSResponse.class);
        WSResponse translationsResp = mock(WSResponse.class);

        // Supported languages: 4
        JsonNode languagesJson = MAPPER.readTree("""
            [
              {"iso_639_1":"en"},
              {"iso_639_1":"fr"},
              {"iso_639_1":"es"},
              {"iso_639_1":"de"}
            ]
        """);

        // Translations: 3 entries, only 2 non-empty overviews.
        // Baseline = longest non-empty overview length = 20
        // Localization ratios: 20/20=1.0, 10/20=0.5 => avg = 0.75
        JsonNode translationsJson = MAPPER.readTree("""
            {
              "id": 27205,
              "translations": [
                {"iso_639_1":"en","data":{"overview":"AAAAAAAAAAAAAAAAAAAA"}},
                {"iso_639_1":"fr","data":{"overview":"BBBBBBBBBB"}},
                {"iso_639_1":"es","data":{"overview":""}}
              ]
            }
        """);

        when(languagesResp.asJson()).thenReturn(languagesJson);
        when(translationsResp.asJson()).thenReturn(translationsJson);

        when(languagesReq.get()).thenReturn(CompletableFuture.completedFuture(languagesResp));
        when(translationsReq.get()).thenReturn(CompletableFuture.completedFuture(translationsResp));

        // --- Run
        GlobalDiversityService service = new GlobalDiversityService(ws, cfg);

        GlobalDiversityStats stats = service.compute("movie", 27205)
                .toCompletableFuture()
                .get();

        // --- Assert
        assertEquals("movie", stats.category);
        assertEquals(27205, stats.id);

        assertEquals(3, stats.translationCount);
        assertEquals(4, stats.supportedLanguageCount);

        assertEquals(0.75, stats.translationDensity, 1e-9);
        assertEquals(0.75, stats.localizationIndex, 1e-9);

        // --- Verify no extra calls
        verify(languagesReq, times(1)).get();
        verify(translationsReq, times(1)).get();
    }

    /** Tests behavior for an invalid category (should return zeros). */
    @Test
    public void testInvalidCategoryReturnsZeros() throws Exception {
        WSClient ws = mock(WSClient.class);
        TmdbConfig cfg = mock(TmdbConfig.class);

        when(cfg.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        when(cfg.getApiKey()).thenReturn("dummy");

        // For invalid category, translations stage returns empty without calling WS.
        // We still expect languages to be fetched. We'll mock it to 2.
        WSRequest languagesReq = mock(WSRequest.class);
        WSResponse languagesResp = mock(WSResponse.class);

        when(ws.url(contains("/configuration/languages"))).thenReturn(languagesReq);

        JsonNode languagesJson = MAPPER.readTree("""
            [
              {"iso_639_1":"en"},
              {"iso_639_1":"fr"}
            ]
        """);

        when(languagesResp.asJson()).thenReturn(languagesJson);
        when(languagesReq.get()).thenReturn(CompletableFuture.completedFuture(languagesResp));

        GlobalDiversityService service = new GlobalDiversityService(ws, cfg);

        GlobalDiversityStats stats = service.compute("person", 1)
                .toCompletableFuture()
                .get();

        assertEquals("person", stats.category);
        assertEquals(1, stats.id);
        assertEquals(0, stats.translationCount);
        assertEquals(2, stats.supportedLanguageCount);
        assertEquals(0.0, stats.translationDensity, 1e-9);
        assertEquals(0.0, stats.localizationIndex, 1e-9);

        verify(languagesReq, times(1)).get();
    }

    @Test
    public void testEncode_withReflection() throws Exception {
        var method = GlobalDiversityService.class
                .getDeclaredMethod("encode", String.class);

        method.setAccessible(true);

        String result1 = (String) method.invoke(null, new Object[]{null});
        assertEquals("", result1);

        String result2 = (String) method.invoke(null, "hello world");
        assertEquals("hello+world", result2);
    }


    @Test
    public void testSafeText_withReflection() throws Exception {
        var method = GlobalDiversityService.class
                .getDeclaredMethod("safeText", JsonNode.class);

        method.setAccessible(true);

        ObjectMapper mapper = new ObjectMapper();

        String result1 = (String) method.invoke(null, new Object[]{null});
        assertEquals("", result1);

        JsonNode nullNode = mapper.nullNode();
        String result2 = (String) method.invoke(null, nullNode);
        assertEquals("", result2);

        JsonNode textNode = TextNode.valueOf("hello");
        String result3 = (String) method.invoke(null, textNode);
        assertEquals("hello", result3);
    }
}