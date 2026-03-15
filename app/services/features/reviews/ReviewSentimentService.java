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
 * ReviewSentiment individual part
 */
public class ReviewSentimentService {

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

    // list of 'positive/happy' keywords
    private static final Set<String> posList = Set.of(
        "excellent", "amazing", "fantastic", "incredible", "outstanding", 
        "brilliant", "masterpiece", "phenomenal", "superb", "magnificent", 
        "exceptional", "perfect", "spectacular", "great", "good", "enjoyable", 
        "entertaining", "fun", "solid", "impressive", "well-made", "memorable", 
        "engaging", "captivating", "effective", "clever", "compelling", "charming",
        "recommend", "loved", "liked", "enjoyed", "recommended", "worth", "hype",
        "worked", "delivered", "decent", "pleasant", "nice", "mark", "best",
        "satisfying", "interesting", "watchable", "likable", "competent", "success",
        "successfully", "successful", "love", "enjoy", "blast", "funny", "wonderful", 
        "wonderfully"
    );

    // list of 'negative/sad' keywords
    private static final Set<String> negList = Set.of( 
        "terrible", "awful", "horrible", "abysmal", "atrocious", "dreadful", 
        "disaster", "garbage", "unwatchable", "painful", "worst", "trash", 
        "bad", "poor", "weak", "boring", "predictable", "disappointing", 
        "forgettable", "mediocre", "flawed", "messy", "slow", "forced", 
        "cliché", "cliche", "average", "bland", "unremarkable", "underwhelming", 
        "uneven", "thin", "inconsistent", "hated", "disliked", "regretted", 
        "flat", "failed", "hate", "flaw", "irk", "annoy", "annoyed", "annoying"
    );

    // list of negating words
    private static final Set<String> negations = Set.of(
        "not", "never", "no", "hardly", "barely", "isnt", "wasnt", "didnt", 
        "wouldnt", "hasnt"
    );

    @Inject
    public ReviewSentimentService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

    /**
     * @author Craig Kogan (40175780)
     * @param kind The kind of media being analyzed. Must be either 'movie' or 'tv'.
     * @param id The id of the movie or tv show
     * @return Returns a completion stage that contains an array list of all the reviews
     * Since each page of reviews contains a maximum of 20 reviews, we need to do three API calls
     * to fetch the first three pages of reviews.
     */
    public CompletionStage<ArrayList<String>> fetchReviewsList(String kind, int id) {
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

        return request1.get().thenCompose(resp1
                -> request2.get().thenCompose(resp2
                        -> request3.get().thenApply(resp3 -> {
                    ArrayList<String> list = new ArrayList<>();
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
                            if (list.size() == 50) {
                                break;
                            }
                        }
                    }
                    return list;
                })
                )
        );
    }

    /**
     * @author Craig Kogan (40175780)
     * @param reviewsList The list of reviews that are to be analyzed
     * @return An emoticon representing the review sentiment
     */
    public Review extractSentiment(ArrayList<String> reviewsList, int id) {
        int posScore = 0;
        int negScore = 0;
        int neutralScore = 0;

        int score;
        String[] split;
        ArrayList<String> sentiments = new ArrayList<>();
        String overall = ":|";

        boolean negate = false;


        // Since streams can only return a single value but I want 4 distinct counters, I'm packing the 
        // counters into a single value. This is a type of encoding. This works because I know that there 
        //are at most 50 reviews. Therefore, no counter will ever surpass 2 digits. That means I need a number
        // that has at most 8 digits. I assign the first two to count the positive reviews,
        // the third and fourth to track the negative reviews, the fifth and sixth to track the
        // neutral reviews, and finally the rightmost two digits track empty reviews (no text).

        // (You can make this type of packing even denser if you use binary bits instead of base10 
        // integer digits because computers store data in bits, so the difference in accuracy
        // will build up with large values when using integer digits)

        // I am aware that this is likely not what the prof had in mind. But, I am undoubtedly using
        // streams to process the data and it works, so I see no problems. 


        /*
        int out1 = reviewsList.stream()
                .mapToInt(r -> {
                    List<String> split2 = Arrays.asList(r.toLowerCase().replaceAll("[^a-zé\\-\\s]", "").split(" "));
                    int scores = 0;
                    // pos: XX------
                    // neg: --XX----
                    // neu: ----XX--
                    // emp: ------XX

                    // s -> score of a single review
                    int s = split2.stream()
                        .mapToInt(w -> {
                            // TODO:: somehow figure out negation words?
                            if (posList.contains(w))
                                return 1;
                            else if (negList.contains(w))
                                return -1;
                            return 0;
                        })
                        .sum();

                    // collect the score of the singular review into a counter
                    // empty
                    if (split2.isEmpty()) // not actually using this :P
                        scores += 1;

                    // positive
                    else if (s > 0)
                        scores += 1000000;

                    // negactive
                    else if (s < 0)
                        scores += 10000;

                    // neutral
                    else 
                        scores += 100;

                    return scores;
                })
                .sum();

        // System.out.println("o1: " + out1);

        int posS = out1 / 1000000;
        out1 -= (posS * 1000000);
        int negS = out1 / 10000;
        out1 -= (negS * 10000);
        int neuS = out1 / 100;
        out1 -= (neuS * 100);
        int emp = out1;

        int thresh = (int)Math.floor((reviewsList.size() - emp) * 0.7f);
        // debug confirmation line
        // System.out.println("thresh: " + thresh + ", posS: " + posS + ", negS: " + negS + ", neuS: " + neuS);

        if (posS >= thresh)
            return ":-)";
        else if (negS >= thresh)
            return ":-(";
        else
            return ":|";

        */

        for (String review : reviewsList)
        {
            score = 0;
            split = review.toLowerCase().replaceAll("[^a-zé\\-\\s]", "").split(" ");
            
            for (String word : split)
            {
                if (negations.contains(word))
                {
                    negate = true;
                    continue;
                }

                if (posList.contains(word))
                    score += !negate ? 1 : -1;
                else if (negList.contains(word))
                    score += !negate ? -1 : 1;

                negate = false;
            }

            if (score > 0)
            {
                posScore++;
                sentiments.add(":-)");
            }
            else if (score < 0)
            {
                negScore++;
                sentiments.add(":-(");
            }
            else
            {
                neutralScore++;
                sentiments.add(":|");
            } 
        }

        // little debug confirmation
        // System.out.print((posScore + negScore + neutralScore + empty == reviewsList.size()) ? "ALL IS GOOD" : "SOMEHOW THE COUNT IS OFF, AHHHHHHHHHHHHHHHHHHHHHHH");
        int threshold = (int)Math.floor((reviewsList.size()) * 0.7f);
        System.out.println(" - thresh: " + threshold + ", pos: " + posScore + ", neg: " + negScore + ", neutral: " + neutralScore);

        if (posScore >= threshold)
            overall =  ":-)";
        else if (negScore >= threshold)
            overall =  ":-(";
        
        return new Review(id, posScore, negScore, neutralScore, overall, reviewsList, sentiments);
    }

    

}
