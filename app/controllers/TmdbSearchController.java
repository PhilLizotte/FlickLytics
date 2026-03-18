package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import services.features.financial.FinancialPerformanceService;

import models.dto.GlobalDiversityStats;
import services.features.diversity.GlobalDiversityService;
import services.features.personstats.PersonStatsService;
import services.features.readability.ReadabilityService;
import services.features.reviews.ReviewSentimentService;
import models.domain.Review;

import services.tmdb.TmdbSearchService;

/**
 * Controller providing TMDb search and detail endpoints.
 * <p>
 * Exposes endpoints for search, movie/TV details, finances, and the person
 * known-for page.
 * Delegates data fetching and computations to the corresponding services.
 * </p>
 *
 *
 */
public class TmdbSearchController extends Controller {
    private final TmdbSearchService tmdbSearchService;
    private final FinancialPerformanceService fpService;
    private final PersonStatsService personStatsService;
    private final ReadabilityService readabilityService;
    private final GlobalDiversityService globalDiversityService;
    private final ReviewSentimentService reviewSentimentService;

    @Inject
    public TmdbSearchController(
            TmdbSearchService tmdbSearchService,
            FinancialPerformanceService fpService,

            PersonStatsService personStatsService,
            ReadabilityService readabilityService,
            GlobalDiversityService globalDiversityService, ReviewSentimentService reviewSentimentService) {
        this.tmdbSearchService = tmdbSearchService;
        this.fpService = fpService;
        this.personStatsService = personStatsService;
        this.readabilityService = readabilityService;
        this.globalDiversityService = globalDiversityService;
        this.reviewSentimentService = reviewSentimentService;
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public CompletionStage<Result> knownFor(Integer id) {
        return personStatsService.getKnownForPage(id)
                .thenApply(page -> ok(views.html.personKnownFor.render(id, page.getItems(), page.getPopularityStats(),
                        page.getVoteAverageStats(), page.getVoteCountStats())))
                .exceptionally(ex -> internalServerError("Failed to load known-for page"));
    }

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
                    if (!validROI)
                        roiPercent = "Unknown; This movie has no recorded budget.";
                    String financialRating = json.path("financialRating").asText();

                    return ok(views.html.financialPerformance.render(title, netProfit, roiPercent, financialRating));
                });
    }

    public CompletionStage<Result> searchMovieById(int id) {
        return fpService.searchMovieById(id)
                .thenApply((JsonNode json) -> ok(json))
                .exceptionally(ex -> badRequest("Unknown movie ID"));
    }

    public CompletionStage<Result> globalDiversity(String category, Integer id) {
        if (category == null || category.trim().isEmpty()) {
            return CompletableFuture.completedFuture(badRequest("Missing category"));
        }
        if (id == null) {
            return CompletableFuture.completedFuture(badRequest("Missing id"));
        }

        return globalDiversityService.compute(category, id)
                .thenApply((GlobalDiversityStats stats) -> ok(views.html.globalDiversity.render(stats)));
    }

    /**
     * Handles the request to show movie details.
     * Fetches the movie from TMDb, calculates readability scores,
     * and renders the movieDetails view.
     *
     * @param id the unique identifier of the movie
     * @return a CompletionStage that will complete with an HTTP Result rendering
     *         the movieDetails page
     */
    public CompletionStage<Result> movieDetails(Integer id) {
        return tmdbSearchService.movieDetails(id)
                .thenApply(movie -> {
                    double readingScore = readabilityService.calculateFleschReaddingEase(movie.getOverview());

                    double gradeLevel = readabilityService.calculateFleschKincaidGradeLevel(movie.getOverview());

                    return ok(views.html.movieDetails.render(
                            movie,
                            readingScore,
                            gradeLevel));
                });
    }

    /**
     * Handles the request to show TV show details.
     * Fetches the TV show from TMDb, calculates readability scores,
     * and renders the tvDetails view.
     *
     * @param id the unique identifier of the TV show
     * @return a CompletionStage that will complete with an HTTP Result rendering
     *         the tvDetails page
     */
    public CompletionStage<Result> tvDetails(Integer id) {
        return tmdbSearchService.tvDetails(id)
                .thenApply(tvShow -> {
                    double readingScore = readabilityService.calculateFleschReaddingEase(tvShow.getOverview());

                    double gradeLevel = readabilityService.calculateFleschKincaidGradeLevel(tvShow.getOverview());

                    return ok(views.html.tvDetails.render(
                            tvShow,
                            readingScore,
                            gradeLevel));
                });
    }

    public CompletionStage<Result> reviewDetails(String kind, Integer id, String title) {
        return reviewSentimentService.fetchReviews(kind, id)
                .thenApply(reviews -> {
                    return ok(views.html.reviews.render(
                            title,
                            reviews));
                });
    }
}
