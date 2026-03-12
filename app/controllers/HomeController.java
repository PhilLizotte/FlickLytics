package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.mvc.*;
import services.features.financial.FinancialPerformanceService;
import services.tmdb.TmdbSearchService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
    private final TmdbSearchService tmdbSearchService;
    private final FinancialPerformanceService fpService;

    @Inject
    public HomeController(TmdbSearchService tmdbSearchService, FinancialPerformanceService fpService) {
        this.tmdbSearchService = tmdbSearchService;
        this.fpService = fpService;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    /**
     * An action that renders an HTML page displaying financial information for a
     * movie based on its <code>id</code>. This feature is only intended for movies,
     * and does not work with shows or people.
     * 
     * @param id The id of the movie for which the financial information is being returned.
     * @return The status of the request, indicating if it was executed successfully, or if not, the error code.
     */
    
    public CompletionStage<Result> finances(int id) {
        return fpService.searchMovieById(id)
            .handle((json, ex) -> {
                if (ex != null) {
                    return badRequest("Movie not found");
                }

                String title = json.path("title").asText();
                String netProfit = json.path("netProfit").asText();
                boolean validROI = json.path("validROI").asBoolean();
                String roiPercent = json.path("roiPercent").asText() + "%";
                if (!validROI) roiPercent = "Unknown; This movie has no recorded budget.";
                String financialRating = json.path("financialRating").asText();

                return ok(views.html.financialPerformance.render(title, netProfit, roiPercent, financialRating));
            });
    }

    /**
     * An action that returns a collection of movies, shows or people depending on
     * <code>category</code>. The contents of <code>query</code> are split by whitespaces,
     * then used to search for items by matching each individual keywords.
     *
     * @param category The category of item to retrieve
     * @param query A series of keywords separated by spaces to match for items
     * @return 10 items that match both <code>category</code> and <code>query</code>.
     */

    public CompletionStage<Result> search(String category, String query) {
        if (category == null || category.trim().isEmpty()) {
            return CompletableFuture.completedFuture(badRequest("Missing category"));
        }
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(badRequest("Missing query"));
        }

        return tmdbSearchService.search(category, query)
            .thenApply((JsonNode json) -> ok(json))
            .exceptionally(ex -> badRequest("Invalid category"));
    }

    /**
     * An action that retrieves detailed information about a movie given its <code>id</code>.
     *
     * @param id The id of the movie for which the financial information is being returned.
     * @return A json containing all detailed information about the movie.
     */

    public CompletionStage<Result> searchMovieById(int id) {

        return fpService.searchMovieById(id)
            .thenApply((JsonNode json) -> ok(json))
            .exceptionally(ex -> badRequest("Unknown movie ID"));
    }

}
