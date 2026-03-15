package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import services.features.financial.FinancialPerformanceService;
import services.features.personstats.PersonStatsService;
import services.features.readability.ReadabilityService;
import services.tmdb.TmdbSearchService;

public class TmdbSearchController extends Controller {
    private final TmdbSearchService tmdbSearchService;
    private final PersonStatsService personStatsService;
    private final ReadabilityService readabilityService;
    private final FinancialPerformanceService fpService;

    @Inject
    public TmdbSearchController(TmdbSearchService tmdbSearchService, PersonStatsService personStatsService,
                                ReadabilityService readabilityService, FinancialPerformanceService fpService) {
        this.tmdbSearchService = tmdbSearchService;
        this.personStatsService = personStatsService;
        this.readabilityService = readabilityService;
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

    public CompletionStage<Result> knownFor(Integer id) {
        return personStatsService.getKnownForPage(id)
            .thenApply(page -> ok(views.html.personKnownFor.render(id, page.getItems(), page.getPopularityStats(), page.getVoteAverageStats(), page.getVoteCountStats())));
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
     * @author Philippe Lizotte
     * An action that renders an HTML page displaying financial information for a
     * movie based on its <code>id</code>. This feature is only intended for movies,
     * and does not work with shows or people.
     *
     * @param id The id of the movie for which the financial information is being returned.
     * @return The status of the request, indicating if it was executed successfully, or if not, the error code.
     */
    public CompletionStage<Result> finances(int id) {
        return fpService.getMovieFinances(id)
                .handle((json, ex) -> {
                    if (ex != null) {
                        return badRequest("Movie not found");
                    }

                    String title = json.path("title").asText();
                    String netProfit = json.path("netProfit").asText();
                    String roiPercent = json.path("roiPercent").asText() + "%";
                    String financialRating = json.path("financialRating").asText();

                    return ok(views.html.financialPerformance.render(title, netProfit, roiPercent, financialRating));
                });
    }

    // @author: aliiimaher
    public CompletionStage<Result> movieDetails(Integer id) {
        return tmdbSearchService.movieDetails(id)
                .thenApply(movie -> {
                    double readingScore =
                            readabilityService.calculateFleschReaddingEase(movie.getOverview());

                    double gradeLevel =
                            readabilityService.calculateFleschKincaidGradeLevel(movie.getOverview());

                    return ok(views.html.movieDetails.render(
                            movie,
                            readingScore,
                            gradeLevel
                    ));
                })
                .exceptionally(ex -> badRequest("Invalid id"));
    }

    public CompletionStage<Result> tvDetails(Integer id) {
        return tmdbSearchService.tvDetails(id)
                .thenApply(tvShowDTO -> {
                    double readingScore =
                            readabilityService.calculateFleschReaddingEase(tvShowDTO.getOverview());

                    double gradeLevel =
                            readabilityService.calculateFleschKincaidGradeLevel(tvShowDTO.getOverview());

                    return ok(views.html.tvDetails.render(
                            tvShowDTO,
                            readingScore,
                            gradeLevel
                    ));
                })
                .exceptionally(ex -> badRequest("Invalid id"));
    }

}
