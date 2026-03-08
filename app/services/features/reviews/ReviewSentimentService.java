package services.features.reviews;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import java.util.concurrent.CompletionStage;

import java.util.ArrayList;

import services.tmdb.TmdbConfig;

public class ReviewSentimentService {

    private final WSClient ws;
    private final TmdbConfig tmdbConfig;

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


        return request1.get().thenCompose(resp1 -> 
            request2.get().thenCompose(resp2 -> 
                request3.get().thenApply(resp3 -> {
                    ArrayList<String> list = new ArrayList<String>();
                    JsonNode res1 = resp1.asJson().get("results");
                    JsonNode res2 = resp2.asJson().get("results");
                    JsonNode res3 = resp3.asJson().get("results");

                    if (res1 != null && res1.isArray())
                        for (JsonNode r : res1)
                            list.add(r.get("content").asText());

                    if (res2 != null && res2.isArray())
                        for (JsonNode r : res2)
                            list.add(r.get("content").asText());

                    if (res3 != null && res3.isArray())
                        for (JsonNode r : res3)
                        {
                            list.add(r.get("content").asText());
                            if (list.size() == 50)
                                break;
                        }
                    return list;
                })
            )
        );
    }

    // TODO:: Instead of nodifying the reviews list, maybe I should do the parsing here as well instead of on the javascript side?
    public String extractSentiment(ArrayList<String> reviewsList)
    {
        // if (reviewsList.size() > 0)
        // {
        //     for (String rev : reviewsList)
        //     {
        //         revs.add(rev);
        //     }
        // }
        // return revs;

        return ":-)";
    }
}
