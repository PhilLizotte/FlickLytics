package controllers;

import jakarta.inject.Inject;
import play.mvc.*;
import services.features.financial.FinancialPerformanceService;
import services.tmdb.TmdbSearchService;

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

    public CompletionStage<Result> finances(int id) {
        return fpService.searchMovieById(id)
            .handle((json, ex) -> {
                if (ex != null) {
                    return badRequest("Movie not found");
                }

                String title = json.path("title").asText();
                String netProfit = json.path("netProfit").asText();
                boolean validROI = json.path("validROI").asBoolean();
                String roiPercent = json.path("roiPercent").asText();
                if (!validROI) roiPercent = "undefined";
                String financialRating = json.path("financialRating").asText();

                return ok(views.html.financialPerformance.render(title, netProfit, roiPercent, financialRating));
            });
    }

}
