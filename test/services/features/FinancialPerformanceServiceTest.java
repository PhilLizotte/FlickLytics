package services.features;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import services.features.financial.FinancialPerformanceService;
import services.tmdb.TmdbConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.pekko.actor.typed.*;

/**
 * Test suite for testing services/financial/FinancialPerformanceService in the main app
 * 
 * @author Philippe Lizotte
 */
public class FinancialPerformanceServiceTest extends WithApplication {

    private WSClient ws;
    private WSRequest request;
    private WSResponse response;
    private TmdbConfig config;

    private FinancialPerformanceService service;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Basic wrapper function that sets up mock services and an instance of FinancialPerformanceService using these mock-ups.
     */
    @Before
    public void setup() {
        ws = mock(WSClient.class);
        request = mock(WSRequest.class);
        response = mock(WSResponse.class);
        config = mock(TmdbConfig.class);

        service = new FinancialPerformanceService(ws, config);
    }
    
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }


    /**
     * Tests the method getMovieFinances under two test cases:
     * The first test case is for when the budget is greater than zero,
     * allowing the ROI percent to be calculable. The second test case is
     * for when the budget is zero.
     *
     * @throws Exception Exception is thrown if a fault occurs within the object mapper.
     */
    @Test
    public void testGetMovieFinances() throws Exception {

        // --- Arrange ---
        when(config.getBaseUrl()).thenReturn("https://api.test.com");
        when(config.getRaToken()).thenReturn("fakeToken");

        JsonNode apiJson = mapper.readTree("""
        {
          "title": "TestMovie",
          "budget": 500000,
          "revenue": 1500000
        }
        """);

        when(response.asJson()).thenReturn(apiJson);

        when(ws.url(anyString())).thenReturn(request);
        when(request.addHeader(anyString(), anyString())).thenReturn(request);
        when(request.get()).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        // --- Act ---
        JsonNode result = service.getMovieFinances(100)
                .toCompletableFuture()
                .join();

        // --- Assert ---
        assertEquals("TestMovie", result.get("title").asText());

        // netProfit = 1,500,000 - 500,000 = 1,000,000
        assertEquals(1000000, result.get("netProfit").asInt());

        // ROI = 200.00
        assertEquals("200.00", result.get("roiPercent").asText());

        // ROI = 200 → "High Return" (based on your logic)
        assertEquals("High Return", result.get("financialRating").asText());

        verify(ws).url("https://api.test.com/movie/100");
        
        // Zero budget edge case
        apiJson = mapper.readTree("""
        {
          "title": "TestMovie",
          "budget": 0,
          "revenue": 1000000
        }
        """);

        when(response.asJson()).thenReturn(apiJson);
        when(ws.url(anyString())).thenReturn(request);
        when(request.addHeader(anyString(), anyString())).thenReturn(request);
        when(request.get()).thenReturn(
                CompletableFuture.completedFuture(response)
        );

        result = service.getMovieFinances(1)
                .toCompletableFuture()
                .join();

        assertEquals("Unknown", result.get("financialRating").asText());
        assertTrue(result.get("roiPercent").asText().contains("Unknown"));
    }
}
