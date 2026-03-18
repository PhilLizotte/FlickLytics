package services.features.reviews;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import java.util.concurrent.CompletionStage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
// import java.util.Collection.stream;
import java.util.stream.Collectors;

import models.domain.Review;

import services.tmdb.TmdbConfig;

/**
 * @author Craig Kogan (40175780)
 *         ReviewSentiment individual part
 */
public class ReviewSentimentService {

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    // list of 'positive/happy' keywords
    private static final Set<String> posList = Set.of(
            "amazing", "best", "blast", "brilliant", "captivating",
            "charming", "clever", "compelling", "competent", "decent",
            "delivered", "effective", "engaging", "enjoy", "enjoyable",
            "enjoyed", "entertaining", "exceptional", "excellent", "fantastic",
            "fun", "funny", "good", "great", "hype",
            "impressive", "incredible", "interesting", "likable", "liked",
            "love", "loved", "magnificent", "mark", "masterpiece",
            "memorable", "nice", "outstanding", "perfect", "phenomenal",
            "pleasant", "recommend", "recommended", "satisfying", "solid",
            "spectacular", "success", "successful", "successfully", "superb",
            "watchable", "well-made", "wonderful", "wonderfully", "worth",
            "worked");

    // list of 'negative/sad' keywords
    private static final Set<String> negList = Set.of(
            "abysmal", "annoy", "annoyed", "annoying", "atrocious", "awful",
            "average", "bad", "bland", "boring", "cliche", "cliché",
            "disappointing", "disaster", "disliked", "dreadful", "failed", "flat",
            "flaw", "flawed", "forgettable", "forced", "garbage", "hate",
            "hated", "horrible", "inconsistent", "irk", "mediocre", "messy",
            "painful", "poor", "predictable", "regretted", "slow", "terrible",
            "thin", "trash", "underwhelming", "uneven", "unremarkable", "unwatchable",
            "weak", "worst");

    // list of negating words
    private static final Set<String> negations = Set.of(
            "barely", "cant", "didnt", "hardly", "hasnt",
            "isnt", "never", "no", "not", "wasnt", "wont", "wouldnt");

    /**
     * Constructor
     * 
     * @param ws         tmdb API request endpoint
     * @param tmdbConfig Configuration parameters
     */
    @Inject
    public ReviewSentimentService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    /**
     * @author Craig Kogan (40175780)
     * @param kind The kind of media. Accepted inputs are movie and tv
     * @param id   The id of the movie/tv show. UNUSED
     * @return A CompletionStage of the complete {@link models.domain.Review Review}
     *         object
     * 
     *         Since each page of reviews contains a maximum of 20 reviews, we need
     *         to do three API calls
     *         to fetch the first three pages of reviews.
     */
    public CompletionStage<Review> fetchReviews(String kind, int id) {
        String url = tmdbConfig.getBaseUrl() + "/" + kind + "/" + id + "/reviews";
        WSRequest request1 = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("language", "en-us")
                .addQueryParameter("page", "1");

        WSRequest request2 = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("language", "en-us")
                .addQueryParameter("page", "2");

        WSRequest request3 = ws.url(url)
                .addQueryParameter("api_key", tmdbConfig.getApiKey())
                .addQueryParameter("language", "en-us")
                .addQueryParameter("page", "3");

        return request1.get()
                .thenCompose(resp1 -> request2.get().thenCompose(resp2 -> request3.get().thenApply(resp3 -> {
                    List<String> list = new ArrayList<>();
                    JsonNode res1 = resp1.asJson().get("results");
                    JsonNode res2 = resp2.asJson().get("results");
                    JsonNode res3 = resp3.asJson().get("results");

                    if (res1 != null && res1.isArray()) {
                        for (JsonNode r : res1) {
                            list.add(r.get("content").asText());
                        }
                    }

                    if (res2 != null && res2.isArray()) {
                        for (JsonNode r : res2) {
                            list.add(r.get("content").asText());
                        }
                    }

                    if (res3 != null && res3.isArray()) {
                        for (JsonNode r : res3) {
                            list.add(r.get("content").asText());
                            if (list.size() >= 50) {
                                break;
                            }
                        }
                    }
                    return extractSentiment(list, id);
                })));
    }

    /**
     * @author Craig Kogan (40175780)
     * @param reviewsList The list of reviews that are to be analyzed
     * @param id          The id of the movie/tv show UNUSED
     * @return The complete {@link models.domain.Review Review} object
     * 
     *         This is the core of the sentiment service. The method parses all the
     *         reviews and determines the sentiment of each, as well as the overall
     *         sentiment.
     */
    public Review extractSentiment(List<String> reviewsList, int id) {
        int posScore = 0;
        int negScore = 0;
        int neutralScore = 0;

        int score;
        String[] split;
        ArrayList<String> sentiments = new ArrayList<>();
        String overall = ":|";

        boolean negate = false;

        // This should never happen. The only way this condition will ever be triggered
        // is if tmdb changes
        // the return of the review query, making it so that each page contains strictly
        // more than 25 reviews
        // if (reviewsList.size() > 50)
        // {
        // System.err.println("SOMEHOW MORE THAN 50 REVIEWS WERE CAPTURED");
        // reviewsList = reviewsList.subList(0, 50);
        // }

        // If there are no reviews, skip the parsing. The sentiment is neutral.
        if (!reviewsList.isEmpty()) {
            // INFO:: this stream is the same as the FOR loop below, except that negations
            // are not calculated.
            // List<String> sents = reviewsList.stream()
            // .map(r -> {
            // // List<String> revs =
            // Arrays.asList(r.toLowerCase().replaceAll("[^a-zé\\-\\s]", "").split(" "));

            // int points = Arrays.asList(r.toLowerCase().replaceAll("[^a-zé\\-\\s]",
            // "").split(" ")).stream()
            // .mapToInt(w -> {
            // // TODO:: somehow figure out negation words?
            // if (posList.contains(w))
            // return 1;
            // else if (negList.contains(w))
            // return -1;
            // return 0;
            // })
            // .sum();

            // if (points > 0)
            // return ":-)";
            // else if (points < 0)
            // return ":-(";
            // return ":|";

            // })
            // .collect(Collectors.toList());

            for (String review : reviewsList) {
                score = 0;
                split = review.toLowerCase().replaceAll("[^a-zé\\-\\s]", "").split(" ");

                for (String word : split) {
                    if (negations.contains(word)) {
                        negate = true;
                        continue;
                    }

                    if (posList.contains(word))
                        score += !negate ? 1 : -1;
                    else if (negList.contains(word))
                        score += !negate ? -1 : 1;

                    negate = false;
                }

                if (score > 0) {
                    posScore++;
                    sentiments.add(":-)");
                } else if (score < 0) {
                    negScore++;
                    sentiments.add(":-(");
                } else {
                    neutralScore++;
                    sentiments.add(":|");
                }
            }

            // little debug confirmation
            int threshold = (int) Math.floor((reviewsList.size()) * 0.7f);
            // We know that there is at least 1 review because if there are no reviews then
            // neutral is automatically returned.
            // Problems happen if the threshold is 0, namely, everything will become
            // positive.
            // We therefore apply a lower bound, ensuring that the threshold is at least 1.
            if (threshold < 1)
                threshold = 1;

            if (posScore >= threshold)
                overall = ":-)";
            else if (negScore >= threshold)
                overall = ":-(";
        }
        return new Review(id, posScore, negScore, neutralScore, overall, reviewsList, sentiments);
    }

}
