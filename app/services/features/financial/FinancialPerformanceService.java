package services.features.financial;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import services.tmdb.TmdbConfig;

import java.util.concurrent.CompletionStage;

public class FinancialPerformanceService {
    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    @Inject
    public FinancialPerformanceService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    public CompletionStage<JsonNode> searchMovieById(int id) {
        
        String url = tmdbConfig.getBaseUrl() + "/movie/" + id;

        return ws.url(url)
                .addHeader("Authorization", "Bearer " + tmdbConfig.getRaToken())
                .get()
                .thenApply(WSResponse::asJson);
    }
}