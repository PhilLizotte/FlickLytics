package services.features.reviews;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import java.util.concurrent.CompletionStage;

import java.util.ArrayList;
import java.util.Set;

import services.tmdb.TmdbConfig;

public class ReviewSentimentService {

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

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

    private static final Set<String> negList = Set.of( 
        "terrible", "awful", "horrible", "abysmal", "atrocious", "dreadful", 
        "disaster", "garbage", "unwatchable", "painful", "worst", "trash", 
        "bad", "poor", "weak", "boring", "predictable", "disappointing", 
        "forgettable", "mediocre", "flawed", "messy", "slow", "forced", 
        "cliché", "cliche", "average", "bland", "unremarkable", "underwhelming", 
        "uneven", "thin", "inconsistent", "hated", "disliked", "regretted", 
        "flat", "failed", "hate", "flaw", "irk", "annoy", "annoyed", "annoying"
    );

    private static final Set<String> negations = Set.of(
        "not", "never", "no", "hardly", "barely", "isnt", "wasnt", "didnt", 
        "wouldnt", "hasnt"
    );

    @Inject
    public ReviewSentimentService(WSClient ws, TmdbConfig tmdbConfig) {
        this.ws = ws;
        this.tmdbConfig = tmdbConfig;
    }

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
                    ArrayList<String> list = new ArrayList<String>();
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

    // BUG:: This is likely an extrmely slow process. 
    public String extractSentiment(ArrayList<String> reviewsList) {
        int posScore = 0;
        int negScore = 0;
        int neutralScore = 0;
        int empty = 0;

        int score;
        String[] split;

        boolean negate = false;

        if (reviewsList.size() == 0)
            return "? (no reviews!)";

        for (String review : reviewsList)
        {
            score = 0;
            split = review.toLowerCase().replaceAll("[^a-zé\\-\\s]", "").split(" ");
            if (split.length == 0)
            {
                empty++;
                continue;
            }
            
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
                posScore++;
            else if (score < 0)
                negScore++;
            else 
                neutralScore++;
        }

        // little debug confirmation
        // System.out.print((posScore + negScore + neutralScore + empty == reviewsList.size()) ? "ALL IS GOOD" : "SOMEHOW THE COUNT IS OFF, AHHHHHHHHHHHHHHHHHHHHHHH");
        int threshold = (int)Math.floor((reviewsList.size() - empty) * 0.7f);
        // System.out.println(" - thresh: " + threshold + ", pos: " + posScore + ", neg: " + negScore + ", neutral: " + neutralScore + ", empty: " + empty);

        if (posScore >= threshold)
            return ":-)";
        else if (negScore >= threshold)
            return ":-(";
        return ":|";
    }
}
