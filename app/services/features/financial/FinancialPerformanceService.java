package services.features.financial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Props;
import play.api.mvc.Result;
import play.libs.ws.WSClient;

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
     * with several extra fields, those being the financial performance metrics.
     */
    public CompletionStage<ObjectNode> getMovieFinances(int id) {
        
        // TO-DO: look at Main branch to see hou Ali did it.
        String url = tmdbConfig.getBaseUrl() + "/movie/" + id;

        return ws.url(url)
            .addHeader("Authorization", "Bearer " + tmdbConfig.getRaToken())
            .get()
            .thenApply(response -> {
                return (ObjectNode) response.asJson();
            });
    }
}