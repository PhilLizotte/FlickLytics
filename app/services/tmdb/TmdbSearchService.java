package services.tmdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import models.domain.Movie;
import models.domain.Review;
import models.domain.TVShow;
import models.dto.MovieDTO;
import models.dto.TVShowDTO;
import models.mapper.TmdbMapper;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import java.util.ArrayList;

import services.features.reviews.ReviewSentimentService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TmdbSearchService {
    private static final long GENRE_CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L;

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;
    private final ObjectMapper objectMapper;

    private final ReviewSentimentService reviewService;

    private final ConcurrentMap<String, GenreCacheEntry> genreCache = new ConcurrentHashMap<>();

    @Inject
    public TmdbSearchService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
        this.objectMapper = Json.mapper();

        this.reviewService = new ReviewSentimentService(ws, tmdbConfig);
    }

    public CompletionStage<JsonNode> search(String category, String query) {
        String endpoint;
        switch (category.toLowerCase()) {
            case "movie" -> endpoint = "/search/movie";
            case "tv" -> endpoint = "/search/tv";
            case "person" -> endpoint = "/search/person";
            default -> {
                return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid category"));
            }
        }

        CompletionStage<Map<Integer, String>> genreMapStage;
        if ("movie".equalsIgnoreCase(category)) {
            genreMapStage = fetchGenreMap("movie");
        } else if ("tv".equalsIgnoreCase(category)) {
            genreMapStage = fetchGenreMap("tv");
        } else {
            genreMapStage = CompletableFuture.completedFuture(Map.of());
        }

        String url = tmdbConfig.getBaseUrl() + endpoint;
        WSRequest request = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("query", query)
                .addQueryParameter("include_adult", "false")
                .addQueryParameter("language", "en-US")
                .addQueryParameter("page", "1");

        return genreMapStage.thenCompose(genreMap -> request.get().thenApply(resp -> {
            JsonNode json = resp.asJson();
            return enrichResults(category, json, genreMap);
        }));
    }

    private CompletionStage<Map<Integer, String>> fetchGenreMap(String kind) {
        GenreCacheEntry cached = genreCache.get(kind);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(cached.map);
        }

        String url = tmdbConfig.getBaseUrl() + "/genre/" + kind + "/list";
        WSRequest request = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("language", "en-US");

        return request.get().thenApply(resp -> {
            JsonNode json = resp.asJson();
            Map<Integer, String> map = new HashMap<>();
            JsonNode genresNode = json.get("genres");
            if (genresNode != null && genresNode.isArray()) {
                for (JsonNode g : genresNode) {
                    JsonNode idNode = g.get("id");
                    JsonNode nameNode = g.get("name");
                    if (idNode != null && idNode.isInt() && nameNode != null && nameNode.isTextual()) {
                        map.put(idNode.asInt(), nameNode.asText());
                    }
                }
            }

            genreCache.put(kind, new GenreCacheEntry(Map.copyOf(map), System.currentTimeMillis()));
            return map;
        });
    }

    /**
     * Fetches the details of a movie from TMDb API by its ID.
     *
     * @param id the unique identifier of the movie
     * @return a {@link CompletionStage} that will complete with the {@link Movie} domain object
     */
    public CompletionStage<Movie> movieDetails(int id) {
        String url = tmdbConfig.getBaseUrl() + "/movie/" + id;
        WSRequest request = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("language", "en-US");

        return request.get()
                .thenApply(response -> {
                    MovieDTO movieDTO = Json.fromJson(response.asJson(), MovieDTO.class);
                    return TmdbMapper.toMovie(movieDTO);
                });
    }

    /**
     * Fetches the details of a TV show from TMDb API by its ID.
     *
     * @param id the unique identifier of the TV show
     * @return a {@link CompletionStage} that will complete with the {@link TVShow} domain object
     */
    public CompletionStage<TVShow> tvDetails(int id) {
        String url = tmdbConfig.getBaseUrl() + "/tv/" + id;
        WSRequest request = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("language", "en-US");

        return request.get()
                .thenApply(response -> {
                    TVShowDTO movieDTO = Json.fromJson(response.asJson(), TVShowDTO.class);
                    return TmdbMapper.toTVShow(movieDTO);
                });
    }

    private static class GenreCacheEntry {
        private final Map<Integer, String> map;
        private final long fetchedAtMillis;

        private GenreCacheEntry(Map<Integer, String> map, long fetchedAtMillis) {
            this.map = map;
            this.fetchedAtMillis = fetchedAtMillis;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - fetchedAtMillis > GENRE_CACHE_TTL_MILLIS;
        }
    }

    private JsonNode enrichResults(String category, JsonNode searchJson, Map<Integer, String> genreMap) {
        if (searchJson == null || !searchJson.isObject()) {
            return searchJson;
        }

        ObjectNode root = ((ObjectNode) searchJson).deepCopy();
        JsonNode resultsNode = root.get("results");
        if (resultsNode == null || !resultsNode.isArray()) {
            return root;
        }

        ArrayNode newResults = objectMapper.createArrayNode();
        for (JsonNode item : resultsNode) {
            if (item == null || !item.isObject()) {
                continue;
            }
            ObjectNode o = ((ObjectNode) item).deepCopy();

            int id = o.hasNonNull("id") ? o.get("id").asInt() : -1;

            CompletionStage<ArrayList<String>> reviewListStage;

            if ("movie".equalsIgnoreCase(category)) {
                o.put("detailsUrl", "https://www.themoviedb.org/movie/" + id);

                // GENRES
                addGenreNames(o, genreMap);

                // BUG:: This is currently blocking code. I'll fix that for a later submission. 
                // REVIEWS
                // HACK:: If anyone knows of a better way to get at the results here, that would
                // be super greatly appreciated.
                // I spent a couple hours trying to figure it out and failed.
                // A big difference between the reviews and the genres is that I need the
                // movie/tv ID to get the reviews,
                // so I can't do it the same way it's done for genres as the ID is only fetched
                // as part of the enrichment process.
                reviewListStage = reviewService.fetchReviewsList("movie", id);
                try {
                    o.put("reviewsSentiment",
                            reviewService.extractSentiment(reviewListStage.toCompletableFuture().get(), id).getOverallSentiment());
                } catch (Exception e) {
                    System.err.println("Error when trying to complete the promise for reviews (movies).");
                }

                normalizeCommonMovieTvFields(o, true);
            } else if ("tv".equalsIgnoreCase(category)) {
                o.put("detailsUrl", "https://www.themoviedb.org/tv/" + id);

                // GENRES
                addGenreNames(o, genreMap);

                // REVIEWS
                reviewListStage = reviewService.fetchReviewsList("tv", id);
                try {
                    o.put("reviewsSentiment",
                            reviewService.extractSentiment(reviewListStage.toCompletableFuture().get(), id).getOverallSentiment());
                } catch (Exception e) {
                    System.err.println("Error when trying to complete the promise for reviews (tv shows).");
                }

                normalizeCommonMovieTvFields(o, false);
            } else if ("person".equalsIgnoreCase(category)) {
                o.put("photoUrl", profileUrl(o));
                o.put("knownForUrl", "https://www.themoviedb.org/person/" + id + "#known_for");
            }

            newResults.add(o);
        }

        root.set("results", newResults);
        return root;
    }

    private void normalizeCommonMovieTvFields(ObjectNode o, boolean movie) {
        if (movie) {
            if (o.hasNonNull("release_date")) {
                o.put("releaseDate", o.get("release_date").asText());
            }
            if (o.hasNonNull("original_language")) {
                o.put("language", o.get("original_language").asText());
            }
        } else {
            if (o.hasNonNull("first_air_date")) {
                o.put("releaseDate", o.get("first_air_date").asText());
            }
            if (o.hasNonNull("original_language")) {
                o.put("language", o.get("original_language").asText());
            }
        }
    }

    private void addGenreNames(ObjectNode o, Map<Integer, String> genreMap) {
        JsonNode idsNode = o.get("genre_ids");
        ArrayNode names = objectMapper.createArrayNode();
        if (idsNode != null && idsNode.isArray()) {
            for (JsonNode idNode : idsNode) {
                if (idNode != null && idNode.isInt()) {
                    String name = genreMap.get(idNode.asInt());
                    if (name != null) {
                        names.add(name);
                    }
                }
            }
        }
        o.set("genres", names);
    }

    private String profileUrl(ObjectNode o) {
        JsonNode pathNode = o.get("profile_path");
        if (pathNode == null || pathNode.isNull() || !pathNode.isTextual()) {
            return "";
        }
        return "https://image.tmdb.org/t/p/original" + pathNode.asText();
    }
}
