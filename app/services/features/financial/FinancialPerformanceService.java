package services.features.financial;

import actors.fpActors.FinancialPerformanceActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import okhttp3.Response;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Props;
import play.libs.ws.WSClient;

import ref.GreeterMain;
import services.tmdb.TmdbConfig;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static org.apache.pekko.pattern.Patterns.ask;

/**
 * A wrapper class to house the <code>getMovieFinances(...)</code> function.
 * @author Philippe Lizotte
 */
public class FinancialPerformanceService {
    private final WSClient ws;
    private final TmdbConfig tmdbConfig;
    private final ActorSystem<FinancialPerformanceActor.GetFPInfo> system;

    @Inject
    public FinancialPerformanceService(WSClient ws, TmdbConfig tmdbConfig, ActorSystem<FinancialPerformanceActor.GetFPInfo> system) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
        this.system = system;
    }

    /**
     * Retrieves the detailed information about a movie and uses it to calculate
     * its financial performance on several metrics (net profit, ROI%, financial rating).
     * 
     * @param id the id of the movie whose information must be retrieved
     * with several extra fields, those being the financial performance metrics.
     */
    public void getMovieFinances(int id) {
        
        // TO-DO: look at Main branch to see hou Ali did it.

        Duration timeout = Duration.ofSeconds(3);
        final ActorSystem<FinancialPerformanceActor.GetFPInfo> fpActor = ActorSystem.create(FinancialPerformanceActor.create(), "fpActor");
        fpActor.tell(new FinancialPerformanceActor.GetFPInfo(id));
        
        /*
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
    */
    }
}