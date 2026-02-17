package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import services.tmdb.TmdbConfig;

import java.util.concurrent.CompletionStage;

public class TmdbSearchController extends Controller {
    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    @Inject
    public TmdbSearchController(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    public CompletionStage<Result> search(String category, String query) {
        if (category == null || category.trim().isEmpty()) {
            return java.util.concurrent.CompletableFuture.completedFuture(badRequest("Missing category"));
        }
        if (query == null || query.trim().isEmpty()) {
            return java.util.concurrent.CompletableFuture.completedFuture(badRequest("Missing query"));
        }

        String endpoint;
        switch (category.toLowerCase()) {
            case "movie" -> endpoint = "/search/movie";
            case "tv" -> endpoint = "/search/tv";
            case "person" -> endpoint = "/search/person";
            default -> {
                return java.util.concurrent.CompletableFuture.completedFuture(badRequest("Invalid category"));
            }
        }

        String url = tmdbConfig.getBaseUrl() + endpoint;
        WSRequest request = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("query", query)
                .addQueryParameter("include_adult", "false")
                .addQueryParameter("language", "en-US")
                .addQueryParameter("page", "1");

        return request.get().thenApply(resp -> {
            JsonNode json = resp.asJson();
            return ok(json);
        });
    }
}
