package actors.diversity;

import models.dto.GlobalDiversityStats;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.features.diversity.GlobalDiversityService;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GlobalDiversityActorTest {

    private static ActorTestKit testKit;

    @BeforeClass
    public static void setup() {
        testKit = ActorTestKit.create("GlobalDiversityActorTest");
    }

    @AfterClass
    public static void teardown() {
        if (testKit != null) {
            testKit.shutdownTestKit();
        }
    }

    @Test
    public void compute_success_movie_repliesOk() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        GlobalDiversityStats stats = new GlobalDiversityStats(
                "movie", 123, 10, 50, 0.2, 0.75
        );

        when(service.compute("movie", 123))
                .thenReturn(CompletableFuture.completedFuture(stats));

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute("movie", 123, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Ok);

        GlobalDiversityActor.Ok ok = (GlobalDiversityActor.Ok) reply;
        assertEquals("movie", ok.stats.category);
        assertEquals(123, ok.stats.id);

        verify(service, times(1)).compute("movie", 123);
    }

    @Test
    public void compute_success_tv_repliesOk() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        GlobalDiversityStats stats = new GlobalDiversityStats(
                "tv", 77, 5, 60, 0.0833, 0.50
        );

        when(service.compute("tv", 77))
                .thenReturn(CompletableFuture.completedFuture(stats));

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute("tv", 77, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Ok);

        GlobalDiversityActor.Ok ok = (GlobalDiversityActor.Ok) reply;
        assertEquals("tv", ok.stats.category);
        assertEquals(77, ok.stats.id);

        verify(service, times(1)).compute("tv", 77);
    }

    @Test
    public void compute_invalidCategory_repliesError_withoutCallingService() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute("person", 1, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Error);

        GlobalDiversityActor.Error err = (GlobalDiversityActor.Error) reply;
        assertEquals("Invalid category. Expected 'movie' or 'tv'.", err.message);

        verify(service, never()).compute(anyString(), anyInt());
    }

    @Test
    public void compute_missingCategory_blank_repliesError_withoutCallingService() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute("   ", 1, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Error);

        GlobalDiversityActor.Error err = (GlobalDiversityActor.Error) reply;
        assertEquals("Missing category", err.message);

        verify(service, never()).compute(anyString(), anyInt());
    }

    @Test
    public void compute_missingCategory_null_repliesError_withoutCallingService() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute(null, 1, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Error);

        GlobalDiversityActor.Error err = (GlobalDiversityActor.Error) reply;
        assertEquals("Missing category", err.message);

        verify(service, never()).compute(anyString(), anyInt());
    }

    @Test
    public void compute_invalidId_repliesError_withoutCallingService() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute("movie", 0, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Error);

        GlobalDiversityActor.Error err = (GlobalDiversityActor.Error) reply;
        assertEquals("Invalid id", err.message);

        verify(service, never()).compute(anyString(), anyInt());
    }

    @Test
    public void compute_serviceFailure_repliesError() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        CompletableFuture<GlobalDiversityStats> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("boom"));

        when(service.compute("movie", 999)).thenReturn(failed);

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute("movie", 999, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Error);

        GlobalDiversityActor.Error err = (GlobalDiversityActor.Error) reply;
        assertEquals("Global diversity computation failed", err.message);

        verify(service, times(1)).compute("movie", 999);
    }

    @Test
    public void compute_serviceReturnsNullStats_repliesError() {
        GlobalDiversityService service = mock(GlobalDiversityService.class);

        when(service.compute("movie", 42))
                .thenReturn(CompletableFuture.completedFuture(null));

        var actor = testKit.spawn(GlobalDiversityActor.create(service));
        TestProbe<GlobalDiversityActor.Response> probe = testKit.createTestProbe();

        actor.tell(new GlobalDiversityActor.Compute("movie", 42, probe.getRef()));

        GlobalDiversityActor.Response reply = probe.receiveMessage();
        assertTrue(reply instanceof GlobalDiversityActor.Error);

        GlobalDiversityActor.Error err = (GlobalDiversityActor.Error) reply;
        assertEquals("Global diversity computation failed", err.message);

        verify(service, times(1)).compute("movie", 42);
    }
}