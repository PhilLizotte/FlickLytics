package services.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import play.Application;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import services.features.financial.FinancialPerformanceService;
import services.tmdb.TmdbConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FinancialPerformanceServiceTest extends WithApplication {
    
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    @Test
    public void testGetMovieFinances() throws Exception {
        // Mock dependencies
        WSClient ws = mock(WSClient.class);
        WSRequest request = mock(WSRequest.class);
        WSResponse response = mock(WSResponse.class);
        TmdbConfig config = mock(TmdbConfig.class);

        when(config.getBaseUrl()).thenReturn("https://api.test.com");
        when(config.getRaToken()).thenReturn("fakeToken");

        // Fake JSON returned by API
        String json = """
        {
            "revenue": 1000000,
            "budget": 500000
        }
        """;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);

        when(response.asJson()).thenReturn(jsonNode);

        // Stub WS chain
        when(ws.url(anyString())).thenReturn(request);
        when(request.addHeader(anyString(), anyString())).thenReturn(request);
        when(request.get()).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        FinancialPerformanceService service =
                new FinancialPerformanceService(ws, config);

        CompletionStage<JsonNode> resultStage =
                service.getMovieFinances(123);

        JsonNode result = resultStage.toCompletableFuture().join();

        // Assertions
        assertEquals(500000, result.get("netProfit").asInt());
        assertEquals(100.0, result.get("roi").asDouble());
    }
}
