package services.features.financial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import play.libs.ws.WSClient;

import services.tmdb.TmdbConfig;

import java.util.concurrent.CompletionStage;

/**
 * A wrapper class to house the <code>getMovieFinances(...)</code> function.
 * @author Philippe Lizotte
 */
public class FinancialPerformanceService {
    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    @Inject
    public FinancialPerformanceService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    /**
     * Retrieves the detailed information about a movie and uses it to calculate
     * its financial performance on several metrics (net profit, ROI%, financial rating).
     * 
     * @param id the id of the movie whose information must be retrieved
     * @return the <code><CompletonStage<JsonNode>/code> of the retrieved JSON objects,
     * with several extra fields, those being the financial performance metrics.
     */
    public CompletionStage<JsonNode> getMovieFinances(int id) {
        
        String url = tmdbConfig.getBaseUrl() + "/movie/" + id;

        return ws.url(url)
            .addHeader("Authorization", "Bearer " + tmdbConfig.getRaToken())
            .get()
            .thenApply(response -> {
                // Calculate extra fields
                ObjectNode json = (ObjectNode) response.asJson();
                int budget = json.get("budget").asInt();
                int revenue = json.get("revenue").asInt();
                
                int netProfit = revenue - budget;
                String roiPercent;
                float roiPercentNum;
                String financialRating;
                if (budget == 0) {
                    roiPercent = "Unknown; This movie has no recorded budget.";
                    financialRating = "Unknown";
                } else {
                    roiPercentNum = (float) (100 * ((double)netProfit / (double)budget));
                    roiPercent = String.format("%.2f", roiPercentNum);
                    if (roiPercentNum < 0) {
                        financialRating = "Financial Loss";
                    } else if (roiPercentNum < 200) {
                        financialRating = "Profitable";
                    } else if (roiPercentNum < 500) {
                        financialRating = "High Return";
                    } else {
                        financialRating = "Blockbuster Success";
                    }
                }

                // Append extra fields to movie json.
                json.put("netProfit", netProfit);
                json.put("roiPercent", roiPercent);
                json.put("financialRating", financialRating);
                return json;
            });
    }
}