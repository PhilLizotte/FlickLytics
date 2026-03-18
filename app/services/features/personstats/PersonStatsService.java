package services.features.personstats;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.inject.Inject;
import models.dto.PersonKnownForItemDTO;
import models.dto.PersonKnownForPageDTO;
import models.dto.PersonKnownForStatsDTO;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import services.tmdb.TmdbConfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * Service for fetching and computing a person's "known-for" items from TMDb.
 * <p>
 * Fetches combined credits, deduplicates results, sorts by most recent release date,
 * computes summary statistics, and caches results for a short TTL.
 * </p>
 *
 * @author Aram Zand
 */
public class PersonStatsService {

    private static final long CACHE_TTL_MILLIS = 10 * 60 * 1000L;

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    private final ConcurrentMap<Integer, CacheEntry> combinedCreditsCache = new ConcurrentHashMap<>();

    @Inject
    public PersonStatsService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    public CompletionStage<List<PersonKnownForItemDTO>> getKnownForItems(Integer personId) {
        if (personId == null) {
            return CompletableFuture.completedFuture(List.of());
        }

        CacheEntry cached = combinedCreditsCache.get(personId);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture(cached.items);
        }

        String url = tmdbConfig.getBaseUrl() + "/person/" + personId + "/combined_credits";
        WSRequest request = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("language", "en-US");

        return request.get().thenApply(resp -> {
            JsonNode json = resp.asJson();
            List<PersonKnownForItemDTO> items = extractAllKnownForItems(json);
            combinedCreditsCache.put(personId, new CacheEntry(items, System.currentTimeMillis()));
            return items;
        });
    }

    public CompletionStage<PersonKnownForPageDTO> getKnownForPage(Integer personId) {
        return getKnownForItems(personId).thenApply(allItems -> {
            List<PersonKnownForItemDTO> uniqueItems = uniqueItems(allItems);

            PersonKnownForStatsDTO popularityStats = computeDoubleStats(uniqueItems.stream().map(PersonKnownForItemDTO::getPopularity));
            PersonKnownForStatsDTO voteAverageStats = computeDoubleStats(uniqueItems.stream().map(PersonKnownForItemDTO::getVoteAverage));
            PersonKnownForStatsDTO voteCountStats = computeIntStats(uniqueItems.stream().map(PersonKnownForItemDTO::getVoteCount));

            List<PersonKnownForItemDTO> latest50Items = latestItems(uniqueItems, 50);
            return new PersonKnownForPageDTO(latest50Items, popularityStats, voteAverageStats, voteCountStats);
        });
    }

    private List<PersonKnownForItemDTO> extractAllKnownForItems(JsonNode combinedCreditsJson) {
        if (combinedCreditsJson == null || !combinedCreditsJson.isObject()) {
            return List.of();
        }

        ArrayNode cast = (combinedCreditsJson.get("cast") instanceof ArrayNode a) ? a : null;
        ArrayNode crew = (combinedCreditsJson.get("crew") instanceof ArrayNode a) ? a : null;

        Stream<JsonNode> castStream = cast == null ? Stream.empty() : Stream.of(cast).flatMap(a -> streamArray(a));
        Stream<JsonNode> crewStream = crew == null ? Stream.empty() : Stream.of(crew).flatMap(a -> streamArray(a));

        return Stream.concat(castStream, crewStream)
                .filter(Objects::nonNull)
                .filter(JsonNode::isObject)
                .map(this::toKnownForItem)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<PersonKnownForItemDTO> uniqueItems(List<PersonKnownForItemDTO> items) {
        Set<String> seen = new HashSet<>();
        return items.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getId() != null)
                .filter(i -> seen.add(i.getMediaType() + ":" + i.getId()))
                .toList();
    }

    private List<PersonKnownForItemDTO> latestItems(List<PersonKnownForItemDTO> items, int limit) {
        Comparator<PersonKnownForItemDTO> byLatestDateDesc = Comparator
                .comparing((PersonKnownForItemDTO i) -> parseDate(i.getReleaseDate()).orElse(LocalDate.MIN))
                .reversed();

        Set<String> seen = new HashSet<>();

        return items.stream()
                .sorted(byLatestDateDesc)
                .filter(i -> i.getId() != null)
                .filter(i -> seen.add(i.getMediaType() + ":" + i.getId()))
                .limit(limit)
                .toList();
    }

    private Stream<JsonNode> streamArray(ArrayNode arrayNode) {
        List<JsonNode> list = new ArrayList<>();
        arrayNode.forEach(list::add);
        return list.stream();
    }

    private PersonKnownForItemDTO toKnownForItem(JsonNode n) {
        Integer id = n.hasNonNull("id") ? n.get("id").asInt() : null;
        String mediaType = n.hasNonNull("media_type") ? n.get("media_type").asText() : "";

        String title = n.hasNonNull("title") ? n.get("title").asText() :
                (n.hasNonNull("name") ? n.get("name").asText() : "");

        String releaseDate = n.hasNonNull("release_date") ? n.get("release_date").asText() :
                (n.hasNonNull("first_air_date") ? n.get("first_air_date").asText() : "");

        Double popularity = n.hasNonNull("popularity") ? n.get("popularity").asDouble() : null;
        Double voteAverage = n.hasNonNull("vote_average") ? n.get("vote_average").asDouble() : null;
        Integer voteCount = n.hasNonNull("vote_count") ? n.get("vote_count").asInt() : null;

        String tmdbUrl;
        if (id == null) {
            tmdbUrl = "";
        } else if ("tv".equalsIgnoreCase(mediaType)) {
            tmdbUrl = "https://www.themoviedb.org/tv/" + id;
        } else {
            tmdbUrl = "https://www.themoviedb.org/movie/" + id;
        }

        return new PersonKnownForItemDTO(id, mediaType, title, releaseDate, popularity, voteAverage, voteCount, tmdbUrl);
    }

    private Optional<LocalDate> parseDate(String s) {
        if (s == null || s.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private PersonKnownForStatsDTO computeDoubleStats(Stream<Double> values) {
        DoubleSummaryStatistics stats = values
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();

        if (stats.getCount() == 0) {
            return new PersonKnownForStatsDTO(0, null, null, null);
        }

        return new PersonKnownForStatsDTO(stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
    }

    private PersonKnownForStatsDTO computeIntStats(Stream<Integer> values) {
        IntSummaryStatistics stats = values
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .summaryStatistics();

        if (stats.getCount() == 0) {
            return new PersonKnownForStatsDTO(0, null, null, null);
        }

        return new PersonKnownForStatsDTO(stats.getCount(), (double) stats.getMin(), (double) stats.getMax(), stats.getAverage());
    }

    private static class CacheEntry {
        private final List<PersonKnownForItemDTO> items;
        private final long fetchedAtMillis;

        private CacheEntry(List<PersonKnownForItemDTO> items, long fetchedAtMillis) {
            this.items = items;
            this.fetchedAtMillis = fetchedAtMillis;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - fetchedAtMillis > CACHE_TTL_MILLIS;
        }
    }
}
