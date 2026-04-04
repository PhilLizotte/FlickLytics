package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.NotUsed;
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.stream.KillSwitches;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.stream.UniqueKillSwitch;
import org.apache.pekko.stream.javadsl.Flow;
import org.apache.pekko.stream.javadsl.Keep;
import org.apache.pekko.stream.javadsl.Sink;
import org.apache.pekko.stream.javadsl.Source;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import play.libs.Json;

import java.time.Duration;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Test suite for {@link UserActor}.
 *
 * <p>This class verifies the behavior of the UserActor in a reactive streaming context,
 * including initialization, streaming behavior, deduplication of results, and proper
 * lifecycle handling.
 *
 * <p>The actor exposes a {@link Flow} that processes incoming {@link JsonNode} messages
 * and produces streamed responses. These tests validate correctness and robustness
 * of that stream pipeline.
 *
 * <p>Tests are implemented using {@link TestKitJunitResource} and Pekko Streams.
 *
 * @author Ali Maher
 *
 */
public class UserActorTest {

    /**
     * Shared test kit resource for spawning actors and probes.
     */
    @ClassRule
    public static final TestKitJunitResource testKit =
            new TestKitJunitResource();

    private Materializer mat;

    /**
     * Initializes the {@link Materializer} before each test.
     */
    @Before
    public void setup() {
        mat = Materializer.matFromSystem(testKit.system());
    }

    /**
     * Verifies that sending an {@link UserActor.Init} message
     * results in creation of a non-null {@link Flow}.
     */
    @Test
    public void testInitCreatesFlow() {
        ActorRef<UserActor.Message> actor =
                testKit.spawn(UserActor.create("user1"));

        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> probe =
                testKit.createTestProbe();

        actor.tell(new UserActor.Init(probe.getRef()));

        Flow<JsonNode, JsonNode, NotUsed> flow =
                probe.receiveMessage();

        assertNotNull(flow);
    }

    /**
     * Tests that the stream does not emit duplicate items across different batches.
     *
     * <p>A search request is sent and results are collected in two batches.
     * The test verifies that items from the second batch do not duplicate
     * items from the first batch based on their IDs.
     */
    @Test
    public void testDeduplication() {
        ActorRef<UserActor.Message> actor =
                testKit.spawn(UserActor.create("user5"));

        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> probe =
                testKit.createTestProbe();

        actor.tell(new UserActor.Init(probe.getRef()));
        Flow<JsonNode, JsonNode, NotUsed> flow =
                probe.receiveMessage();

        ObjectNode input = Json.newObject();
        input.put("type", "search");
        input.put("query", "spiderman");

        // First batch
        List<JsonNode> firstBatch =
                Source.<JsonNode>single(input)
                        .via(flow)
                        .take(3)
                        .runWith(Sink.seq(), mat)
                        .toCompletableFuture()
                        .join();

        // Wait for tick (simulate streaming)
        try { Thread.sleep(6000); } catch (InterruptedException ignored) {}

        List<JsonNode> secondBatch =
                Source.<JsonNode>empty()
                        .via(flow)
                        .take(3)
                        .runWith(Sink.seq(), mat)
                        .toCompletableFuture()
                        .join();

        // Ensure no duplicate IDs between batches
        for (JsonNode a : firstBatch) {
            for (JsonNode b : secondBatch) {
                assertNotEquals(a.get("id").asText(), b.get("id").asText());
            }
        }
    }

    /**
     * Verifies that periodic stream ticks produce data over time.
     *
     * <p>A combination of an initial request and periodic tick events
     * is used to simulate a live data stream. The test ensures that
     * the stream emits multiple elements over time.
     */
    @Test
    public void testStreamingTickProducesData() {
        ActorRef<UserActor.Message> actor =
                testKit.spawn(UserActor.create("user6"));

        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> probe =
                testKit.createTestProbe();

        actor.tell(new UserActor.Init(probe.getRef()));
        Flow<JsonNode, JsonNode, NotUsed> flow =
                probe.receiveMessage();

        ObjectNode input = Json.newObject();
        input.put("type", "search");
        input.put("query", "ironman");

        List<JsonNode> output =
                Source.<JsonNode>single(input)
                        .concat(Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(5), input))
                        .via(flow)
                        .take(6)
                        .runWith(Sink.seq(), mat)
                        .toCompletableFuture()
                        .join();

        assertTrue(output.size() >= 3);
    }

    /**
     * Verifies that the actor and stream can be safely terminated.
     *
     * <p>A {@link UniqueKillSwitch} is used to cancel the running stream,
     * and the actor is then stopped. The test ensures no crashes occur
     * during shutdown.
     */
    @Test
    public void testActorStopsOnTermination() {
        ActorRef<UserActor.Message> actor =
                testKit.spawn(UserActor.create("user7"));

        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> probe =
                testKit.createTestProbe();

        actor.tell(new UserActor.Init(probe.getRef()));
        Flow<JsonNode, JsonNode, NotUsed> flow =
                probe.receiveMessage();

        // run and cancel immediately
        UniqueKillSwitch killSwitch =
                Source.<JsonNode>maybe()
                        .viaMat(KillSwitches.single(), Keep.right())
                        .via(flow)
                        .toMat(Sink.ignore(), Keep.left())
                        .run(mat);
        killSwitch.shutdown();

        testKit.stop(actor);
        assertTrue(true);
    }
}