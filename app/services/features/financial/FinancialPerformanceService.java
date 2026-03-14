package services.features.financial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import play.mvc.Result;
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
            .thenApply(response -> {
                ObjectNode json = (ObjectNode) response.asJson();
                int budget = json.get("budget").asInt();
                int revenue = json.get("revenue").asInt();
                int netProfit = revenue - budget;
                boolean validROI = true;
                int roiPercent;
                String financialRating;
                if (budget == 0) {
                    validROI = false;
                    roiPercent = 0;
                    financialRating = "Unknown";
                } else {
                    roiPercent = (int) (100 * ((double)netProfit / (double)budget));
                    if (roiPercent < 0) {
                        financialRating = "Financial Loss";
                    } else if (roiPercent < 200) {
                        financialRating = "Profitable";
                    } else if (roiPercent < 500) {
                        financialRating = "High Return";
                    } else {
                        financialRating = "Blockbuster Success";
                    }
                }

                json.put("netProfit", netProfit);
                json.put("validROI", validROI);
                json.put("roiPercent", roiPercent);
                json.put("financialRating", financialRating);
                return json;
            });
    }
}