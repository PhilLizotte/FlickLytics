package services.features.diversity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import models.dto.GlobalDiversityStats;
import play.libs.ws.WSClient;
import services.tmdb.TmdbConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GlobalDiversityService {

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    @Inject
    public GlobalDiversityService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    public CompletionStage<GlobalDiversityStats> compute(String category, int id) {
        CompletionStage<Integer> supportedLanguagesStage = fetchSupportedLanguageCount();
        CompletionStage<TranslationsData> translationsStage = fetchTranslationsData(category, id);

        return supportedLanguagesStage.thenCombine(translationsStage, (supportedCount, tData) -> {
            int translationCount = tData.translationCount;
            int supportedLanguageCount = supportedCount;

            double translationDensity = 0.0;
            if (supportedLanguageCount > 0) {
                translationDensity = (double) translationCount / (double) supportedLanguageCount;
            }

            double localizationIndex = computeLocalizationIndex(tData.originalOverview, tData.translatedOverviews);

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

                    String baselineOverview = "";
                    List<String> overviews = new ArrayList<>();

                    for (JsonNode t : translations) {
                        JsonNode data = t.get("data");
                        if (data == null) continue;

                        String overview = safeText(data.get("overview")).trim();
                        if (overview.isEmpty()) continue;

                        overviews.add(overview);

                        // Choose the longest non-empty overview as baseline
                        if (overview.length() > baselineOverview.length()) {
                            baselineOverview = overview;
                        }
                    }

                    return new TranslationsData(baselineOverview, overviews, translationCount);
                });
    }

    private double computeLocalizationIndex(String baselineOverview, List<String> translatedOverviews) {
        if (baselineOverview == null) baselineOverview = "";
        int baseLen = baselineOverview.trim().length();
        if (baseLen == 0) return 0.0;

        double sum = 0.0;
        int n = 0;

        for (String ov : translatedOverviews) {
            if (ov == null) continue;
            int len = ov.trim().length();
            if (len == 0) continue;

            sum += ((double) len) / ((double) baseLen);
            n++;
        }

        return n == 0 ? 0.0 : (sum / n);
    }

    private static String safeText(JsonNode node) {
        return (node == null || node.isNull()) ? "" : node.asText("");
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static class TranslationsData {
        final String originalOverview;
        final List<String> translatedOverviews;
        final int translationCount;

        TranslationsData(String originalOverview, List<String> translatedOverviews, int translationCount) {
            this.originalOverview = originalOverview;
            this.translatedOverviews = translatedOverviews;
            this.translationCount = translationCount;
        }
    }
}