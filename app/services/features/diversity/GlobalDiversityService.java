package services.features.diversity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import models.dto.GlobalDiversityStats;
import play.libs.ws.WSClient;
import services.tmdb.TmdbConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Computes "Global Diversity" metrics for a TMDb entity (movie or TV show).
 *
 * <p>Metrics produced:
 * <ul>
 *   <li><b>translationCount</b>: number of translation entries returned by TMDb translations endpoint</li>
 *   <li><b>supportedLanguageCount</b>: number of languages supported by TMDb (configuration/languages)</li>
 *   <li><b>translationDensity</b>: translationCount / supportedLanguageCount</li>
 *   <li><b>localizationIndex</b>: average ratio of translated overview length to baseline overview length</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>Non-blocking: uses CompletionStage and never calls join/get.</li>
 *   <li>Business logic lives in the service (not controller), aligned with MVC.</li>
 *   <li>Intended to be unit-tested with mocked WSClient (no real TMDb calls).</li>
 * </ul>
 *
 * @author Chama
 */
public class GlobalDiversityService {

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    /**
     * Creates the service.
     *
     * @param ws Play WS client used for async HTTP calls
     * @param tmdbConfig TMDb configuration (baseUrl + credentials)
     */
    @Inject
    public GlobalDiversityService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    /**
     * Computes global diversity metrics for a given category and TMDb id.
     *
     * @param category "movie" or "tv" (case-insensitive)
     * @param id TMDb entity id
     * @return async result containing {@link GlobalDiversityStats}
     */
    public CompletionStage<GlobalDiversityStats> compute(String category, int id) {
        CompletionStage<Integer> supportedLanguagesStage = fetchSupportedLanguageCount();
        CompletionStage<TranslationsData> translationsStage = fetchTranslationsData(category, id);

        return supportedLanguagesStage.thenCombine(translationsStage, (supportedCount, tData) -> {
            int translationCount = tData.translationCount;
            int supportedLanguageCount = supportedCount;

            double translationDensity =
                    (supportedLanguageCount > 0)
                            ? ((double) translationCount / (double) supportedLanguageCount)
                            : 0.0;

            double localizationIndex =
                    computeLocalizationIndex(tData.baselineOverview, tData.translatedOverviews);

            return new GlobalDiversityStats(
                    category,
                    id,
                    translationCount,
                    supportedLanguageCount,
                    translationDensity,
                    localizationIndex
            );
        });
    }

    /**
     * Fetches the number of languages supported by TMDb.
     *
     * @return async count of supported languages
     */
    private CompletionStage<Integer> fetchSupportedLanguageCount() {
        String url = tmdbConfig.getBaseUrl()
                + "/configuration/languages?api_key="
                + encode(tmdbConfig.getApiKey());

        return ws.url(url)
                .get()
                .thenApply(resp -> {
                    JsonNode json = resp.asJson();
                    return (json != null && json.isArray()) ? json.size() : 0;
                });
    }

    /**
     * Fetches translation data for a given entity (movie or tv).
     *
     * @param category "movie" or "tv"
     * @param id entity id
     * @return async translations data (baseline + list of non-empty overviews + translation count)
     */
    private CompletionStage<TranslationsData> fetchTranslationsData(String category, int id) {
        String path;
        if ("movie".equalsIgnoreCase(category)) {
            path = "/movie/" + id + "/translations";
        } else if ("tv".equalsIgnoreCase(category)) {
            path = "/tv/" + id + "/translations";
        } else {
            return CompletableFuture.completedFuture(new TranslationsData("", List.of(), 0));
        }

        String url = tmdbConfig.getBaseUrl()
                + path
                + "?api_key="
                + encode(tmdbConfig.getApiKey());

        return ws.url(url)
                .get()
                .thenApply(resp -> {
                    JsonNode json = resp.asJson();
                    JsonNode translations = (json == null) ? null : json.get("translations");

                    if (translations == null || !translations.isArray()) {
                        return new TranslationsData("", List.of(), 0);
                    }

                    int translationCount = translations.size();

                    // Collect non-empty overviews
                    List<String> overviews = new ArrayList<>();
                    for (JsonNode t : translations) {
                        JsonNode data = t.get("data");
                        if (data == null) continue;

                        String overview = safeText(data.get("overview")).trim();
                        if (!overview.isEmpty()) {
                            overviews.add(overview);
                        }
                    }

                    // Baseline = longest overview (Streams)
                    Optional<String> baselineOpt = overviews.stream()
                            .max(Comparator.comparingInt(String::length));

                    String baseline = baselineOpt.orElse("");

                    return new TranslationsData(baseline, overviews, translationCount);
                });
    }

    /**
     * Computes localization index as:
     * average( length(overview_i) / length(baseline) ) for non-empty overviews.
     *
     * @param baselineOverview baseline overview text
     * @param translatedOverviews list of translated overviews (non-null list)
     * @return localization index in [0..] (0 if baseline empty or no valid overviews)
     */
    private double computeLocalizationIndex(String baselineOverview, List<String> translatedOverviews) {
        String base = (baselineOverview == null) ? "" : baselineOverview.trim();
        int baseLen = base.length();
        if (baseLen == 0) return 0.0;

        List<Integer> lengths = translatedOverviews.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(s -> s.trim().length())
                .collect(Collectors.toList());

        if (lengths.isEmpty()) return 0.0;

        double avgRatio = lengths.stream()
                .mapToDouble(len -> ((double) len) / ((double) baseLen))
                .average()
                .orElse(0.0);

        return avgRatio;
    }

    private static String safeText(JsonNode node) {
        return (node == null || node.isNull()) ? "" : node.asText("");
    }

    private static String encode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    /** Simple container for translations computation inputs. */
    private static class TranslationsData {
        final String baselineOverview;
        final List<String> translatedOverviews;
        final int translationCount;

        TranslationsData(String baselineOverview, List<String> translatedOverviews, int translationCount) {
            this.baselineOverview = baselineOverview;
            this.translatedOverviews = translatedOverviews;
            this.translationCount = translationCount;
        }
    }
}