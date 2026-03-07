package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import services.tmdb.TmdbSearchService;
import services.features.financial.FinancialPerformanceService;

public class TmdbSearchController extends Controller {
    private final TmdbSearchService tmdbSearchService;
    private final FinancialPerformanceService fpService;

    @Inject
    public TmdbSearchController(TmdbSearchService tmdbSearchService, FinancialPerformanceService fpService) {
        this.tmdbSearchService = tmdbSearchService;
        this.fpService = fpService;
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
    
    public CompletionStage<Result> searchMovieById(int id) {
        
        return fpService.searchMovieById(id)
                .thenApply((JsonNode json) -> ok(json))
                .exceptionally(ex -> badRequest("Unknown movie ID"));
    }
}
