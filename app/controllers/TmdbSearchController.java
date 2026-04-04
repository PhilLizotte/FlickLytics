package controllers;


import actors.readability.ReadabilityActor;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import models.dto.GlobalDiversityStats;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Props;
import org.apache.pekko.actor.typed.Scheduler;
import org.apache.pekko.actor.typed.javadsl.Adapter;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.slf4j.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import services.features.diversity.GlobalDiversityService;
import services.features.financial.FinancialPerformanceService;
import services.features.personstats.PersonStatsService;
import services.features.readability.ReadabilityService;
import services.features.reviews.ReviewSentimentService;
import services.tmdb.TmdbSearchService;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Controller providing TMDb search and detail endpoints.
 * <p>
 * Exposes endpoints for search, movie/TV details, finances, and the person
 * known-for page.
 * Delegates data fetching and computations to the corresponding services.
 * </p>
 *
 * @author all_team_members
 * 
 */
public class TmdbSearchController extends Controller {
    private final TmdbSearchService tmdbSearchService;
    private final FinancialPerformanceService fpService;
    private final PersonStatsService personStatsService;
    private final ReadabilityService readabilityService;
    private final GlobalDiversityService globalDiversityService;
    private final ReviewSentimentService reviewSentimentService;
    // second delivery - gp
    private final ActorSystem actorSystem;
    // second delivery - individual
    private final ActorRef<ReadabilityActor.Command> readabilityActor;
    private final Scheduler scheduler;

    @Inject
    public TmdbSearchController(
            TmdbSearchService tmdbSearchService,
            FinancialPerformanceService fpService,
            PersonStatsService personStatsService,
            ReadabilityService readabilityService,
            GlobalDiversityService globalDiversityService,
            ReviewSentimentService reviewSentimentService,
            ActorSystem classicActorSystem){
        this.tmdbSearchService = tmdbSearchService;
        this.fpService = fpService;
        this.personStatsService = personStatsService;
        this.readabilityService = readabilityService;
        this.globalDiversityService = globalDiversityService;
        this.reviewSentimentService = reviewSentimentService;
        org.apache.pekko.actor.typed.ActorSystem<Void> typedSystem =
                Adapter.toTyped(classicActorSystem);
        this.actorSystem = classicActorSystem;
        this.readabilityActor = typedSystem.systemActorOf(ReadabilityActor.create(readabilityService),
                "readabilityActor", Props.empty());
        this.scheduler = typedSystem.scheduler();
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
     * @author Aram Zand
     * 
     *         Renders the known for page
     * 
     * @param id id of person
     * @return The status of the request
     */
    public CompletionStage<Result> knownFor(Integer id) {
        return personStatsService.getKnownForPage(id)
                .thenApply(page -> ok(views.html.personKnownFor.render(id, page.getItems(), page.getPopularityStats(),
                        page.getVoteAverageStats(), page.getVoteCountStats())));
    }

    /**
     * An action that returns a collection of movies, shows or people depending on
     * <code>category</code>. The contents of <code>query</code> are split by
     * whitespaces,
     * then used to search for items by matching each individual keywords.
     *
     * @param category The category of item to retrieve
     * @param query    A series of keywords separated by spaces to match for items
     * @return 10 items that match both <code>category</code> and
     *         <code>query</code>.
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
     * An action that renders an HTML page displaying financial information for a
     * movie based on its <code>id</code>. This feature is only intended for movies,
     * and does not work with shows or people.
     * 
     * @author Philippe Lizotte
     *
     * @param id The id of the movie for which the financial information is being
     *           returned.
     * @return The status of the request, indicating if it was executed
     *         successfully, or if not, the error code.
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

    /**
     * Searches for movie by id
     * 
     * @author Philippe Lizotte
     * @param id The id of the movie
     * @return Status of the request
     */
    public CompletionStage<Result> searchMovieById(int id) {
        return fpService.getMovieFinances(id)
                .thenApply((JsonNode json) -> ok(json))
                .exceptionally(ex -> badRequest("Unknown movie ID"));
    }

    /**
     * Renders the global diversity page
     * 
     * @author Chama Amri Toudrhi
     * 
     * @param category movie or tv
     * @param id       id of the movie or tv show
     * @return Status of the request
     */
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
     * @author Seyed Ali Mohammad Maher
     *
     * @param id the unique identifier of the movie
     * @return a CompletionStage that will complete with an HTTP Result rendering
     *         the movieDetails page
     */
    public CompletionStage<Result> movieDetails(Integer id) {
        return tmdbSearchService.movieDetails(id)
                .thenCompose(movie ->
                    AskPattern.<ReadabilityActor.Command, ReadabilityActor.Result>ask(
                            readabilityActor,
                            replyTo -> new ReadabilityActor.Compute(movie.getOverview(), replyTo),
                            Duration.ofSeconds(3),
                            scheduler
                        ).thenApply(readability ->
                            ok(views.html.movieDetails.render(
                                    movie,
                                    readability.fleschScore,
                                    readability.gradeLevel
                            ))
                        )
                );
    }

    /**
     * 
     * 
     * Handles the request to show TV show details.
     * Fetches the TV show from TMDb, calculates readability scores,
     * and renders the tvDetails view.
     * 
     * @author Seyed Ali Mohammad Maher
     * 
     * @param id the unique identifier of the TV show
     * @return a CompletionStage that will complete with an HTTP Result rendering
     *         the tvDetails page
     */
    public CompletionStage<Result> tvDetails(Integer id) {
        return tmdbSearchService.tvDetails(id)
                .thenCompose(tvShow ->
                        AskPattern.<ReadabilityActor.Command, ReadabilityActor.Result>ask(
                                readabilityActor,
                                replyTo -> new ReadabilityActor.Compute(tvShow.getOverview(), replyTo),
                                Duration.ofSeconds(3),
                                scheduler
                            ).thenApply(readability ->
                                ok(views.html.tvDetails.render(
                                        tvShow,
                                        readability.fleschScore,
                                        readability.gradeLevel
                                ))
                            )
                );
    }

    /**
     * Renders the review details page
     * 
     * @author Craig Kogan
     * 
     * @param kind  movie or tv
     * @param id    id of movie or tv show
     * @param title title of movie/tv show
     * @return Status of the request
     */
    public CompletionStage<Result> reviewDetails(String kind, Integer id, String title) {
        return reviewSentimentService.fetchReviews(kind, id)
                .thenApply(reviews -> {
                    return ok(views.html.reviews.render(
                            title,
                            reviews));
                });
    }
}