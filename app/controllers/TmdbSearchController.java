package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import services.features.personstats.PersonStatsService;
import services.features.readability.ReadabilityService;
import services.tmdb.TmdbSearchService;

public class TmdbSearchController extends Controller {
    private final TmdbSearchService tmdbSearchService;
    private final PersonStatsService personStatsService;
    private final ReadabilityService readabilityService;

    @Inject
    public TmdbSearchController(TmdbSearchService tmdbSearchService, PersonStatsService personStatsService,
                                ReadabilityService readabilityService) {
        this.tmdbSearchService = tmdbSearchService;
        this.personStatsService = personStatsService;
        this.readabilityService = readabilityService;
    }

    public Result index() {
        return ok(views.html.index.render());
    }

    public CompletionStage<Result> knownFor(Integer id) {
        return personStatsService.getKnownForPage(id)
                .thenApply(page -> ok(views.html.personKnownFor.render(id, page.getItems(), page.getPopularityStats(), page.getVoteAverageStats(), page.getVoteCountStats())));
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
                });
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
                });
    }

}
