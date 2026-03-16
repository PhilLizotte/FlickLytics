package services.features;

import models.dto.PersonKnownForItemDTO;
import models.dto.PersonKnownForPageDTO;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.features.personstats.PersonStatsService;
import services.tmdb.TmdbConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link services.features.personstats.PersonStatsService}.
 * <p>
 * Verifies known-for item extraction, deduplication, sorting, statistics computation,
 * and caching behavior.
 * </p>
 *
 * @author Aram Zand
 */
public class PersonStatsServiceTest {

    @Test
    public void testGetKnownForPageComputesStatsDedupAndSortsByLatestDate() {
        Integer personId = 500;

        WSClient ws = Mockito.mock(WSClient.class);
        WSRequest request = Mockito.mock(WSRequest.class);
        WSResponse response = Mockito.mock(WSResponse.class);

        TmdbConfig tmdbConfig = Mockito.mock(TmdbConfig.class);
        Mockito.when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        Mockito.when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        String expectedUrl = "https://api.themoviedb.org/3/person/" + personId + "/combined_credits";
        Mockito.when(ws.url(expectedUrl)).thenReturn(request);
        Mockito.when(request.addQueryParameter(Mockito.anyString(), Mockito.anyString())).thenReturn(request);

        Mockito.when(response.asJson()).thenReturn(Json.parse("""
            {
              \"cast\": [
                {\"id\": 1, \"media_type\": \"movie\", \"title\": \"A\", \"release_date\": \"2020-01-01\", \"popularity\": 10.0, \"vote_average\": 7.0, \"vote_count\": 100},
                {\"id\": 1, \"media_type\": \"movie\", \"title\": \"A (dup)\", \"release_date\": \"2020-01-01\", \"popularity\": 999.0, \"vote_average\": 9.9, \"vote_count\": 999},
                {\"id\": 2, \"media_type\": \"tv\", \"name\": \"B\", \"first_air_date\": \"2021-05-10\", \"popularity\": 20.0, \"vote_average\": 8.0, \"vote_count\": 200},
                {\"id\": 3, \"media_type\": \"movie\", \"title\": \"C\", \"release_date\": \"\", \"popularity\": null, \"vote_average\": null, \"vote_count\": null}
              ],
              \"crew\": [
                {\"id\": 2, \"media_type\": \"tv\", \"name\": \"B (dup)\", \"first_air_date\": \"2021-05-10\", \"popularity\": 20.0, \"vote_average\": 8.0, \"vote_count\": 200},
                {\"id\": 4, \"media_type\": \"movie\", \"title\": \"D\", \"release_date\": \"2019-12-31\", \"popularity\": 5.0, \"vote_average\": 6.0, \"vote_count\": 50}
              ]
            }
        """));

        Mockito.when(request.get()).thenReturn(CompletableFuture.completedFuture(response));

        PersonStatsService service = new PersonStatsService(ws, tmdbConfig);
        PersonKnownForPageDTO page = service.getKnownForPage(personId).toCompletableFuture().join();

        List<PersonKnownForItemDTO> items = page.getItems();
        assertEquals(4, items.size());

        assertEquals(Integer.valueOf(2), items.get(0).getId());
        assertEquals(Integer.valueOf(1), items.get(1).getId());
        assertEquals(Integer.valueOf(4), items.get(2).getId());
        assertEquals(Integer.valueOf(3), items.get(3).getId());

        assertEquals("https://www.themoviedb.org/tv/2", items.get(0).getTmdbUrl());
        assertEquals("https://www.themoviedb.org/movie/1", items.get(1).getTmdbUrl());
        assertEquals("https://www.themoviedb.org/movie/4", items.get(2).getTmdbUrl());
        assertEquals("https://www.themoviedb.org/movie/3", items.get(3).getTmdbUrl());

        assertEquals(3, page.getPopularityStats().getCount());
        assertEquals(5.0, page.getPopularityStats().getMin(), 0.0001);
        assertEquals(20.0, page.getPopularityStats().getMax(), 0.0001);
        assertEquals((10.0 + 20.0 + 5.0) / 3.0, page.getPopularityStats().getAverage(), 0.0001);

        assertEquals(3, page.getVoteAverageStats().getCount());
        assertEquals(6.0, page.getVoteAverageStats().getMin(), 0.0001);
        assertEquals(8.0, page.getVoteAverageStats().getMax(), 0.0001);
        assertEquals((7.0 + 8.0 + 6.0) / 3.0, page.getVoteAverageStats().getAverage(), 0.0001);

        assertEquals(3, page.getVoteCountStats().getCount());
        assertEquals(50.0, page.getVoteCountStats().getMin(), 0.0001);
        assertEquals(200.0, page.getVoteCountStats().getMax(), 0.0001);
        assertEquals((100.0 + 200.0 + 50.0) / 3.0, page.getVoteCountStats().getAverage(), 0.0001);
    }

    @Test
    public void testGetKnownForItemsUsesCacheOnSecondCall() {
        Integer personId = 500;

        WSClient ws = Mockito.mock(WSClient.class);
        WSRequest request = Mockito.mock(WSRequest.class);
        WSResponse response = Mockito.mock(WSResponse.class);

        TmdbConfig tmdbConfig = Mockito.mock(TmdbConfig.class);
        Mockito.when(tmdbConfig.getBaseUrl()).thenReturn("https://api.themoviedb.org/3");
        Mockito.when(tmdbConfig.getApiKey()).thenReturn("test-api-key");

        String expectedUrl = "https://api.themoviedb.org/3/person/" + personId + "/combined_credits";
        Mockito.when(ws.url(expectedUrl)).thenReturn(request);
        Mockito.when(request.addQueryParameter(Mockito.anyString(), Mockito.anyString())).thenReturn(request);

        Mockito.when(response.asJson()).thenReturn(Json.parse("""
            { "cast": [ {"id": 1, "media_type": "movie", "title": "A", "release_date": "2020-01-01"} ], "crew": [] }
        """));
        Mockito.when(request.get()).thenReturn(CompletableFuture.completedFuture(response));

        PersonStatsService service = new PersonStatsService(ws, tmdbConfig);
        List<PersonKnownForItemDTO> first = service.getKnownForItems(personId).toCompletableFuture().join();
        List<PersonKnownForItemDTO> second = service.getKnownForItems(personId).toCompletableFuture().join();

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        Mockito.verify(request, Mockito.times(1)).get();
    }

    @Test
    public void testGetKnownForPageWithNullPersonIdReturnsEmptyStats() {
        WSClient ws = Mockito.mock(WSClient.class);
        TmdbConfig tmdbConfig = Mockito.mock(TmdbConfig.class);

        PersonStatsService service = new PersonStatsService(ws, tmdbConfig);
        PersonKnownForPageDTO page = service.getKnownForPage(null).toCompletableFuture().join();

        assertEquals(0, page.getItems().size());

        assertEquals(0, page.getPopularityStats().getCount());
        assertNull(page.getPopularityStats().getMin());
        assertNull(page.getPopularityStats().getMax());
        assertNull(page.getPopularityStats().getAverage());

        assertEquals(0, page.getVoteAverageStats().getCount());
        assertNull(page.getVoteAverageStats().getMin());
        assertNull(page.getVoteAverageStats().getMax());
        assertNull(page.getVoteAverageStats().getAverage());

        assertEquals(0, page.getVoteCountStats().getCount());
        assertNull(page.getVoteCountStats().getMin());
        assertNull(page.getVoteCountStats().getMax());
        assertNull(page.getVoteCountStats().getAverage());
    }
}
