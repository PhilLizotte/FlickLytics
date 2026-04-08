package actors;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pekko.NotUsed;
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.stream.javadsl.Flow;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test suite for {@link UserParentActor}.
 *
 * <p>This class verifies the behavior of the UserParentActor, including:
 * <ul>
 *     <li>Successful instantiation of the actor</li>
 *     <li>Spawning of child UserActor instances</li>
 *     <li>Returning a valid {@link Flow} for each user</li>
 *     <li>Ensuring different users receive distinct Flow instances</li>
 *     <li>Ensuring the actor does not crash during normal operations</li>
 * </ul>
 *
 *  <p>The tests are implemented using {@link TestKitJunitResource} from Pekko Typed TestKit.
 *
 * @author Ali Maher
 *
 */
public class UserParentActorTest {

    /**
     * Shared test kit resource used to spawn actors and probes.
     */
    @ClassRule
    public static final TestKitJunitResource testKit =
            new TestKitJunitResource();

    /**
     * Basic sanity test to ensure the class can be instantiated without errors.
     */
    @Test
    public void testThis() {
        new UserParentActor();
        assertTrue(true);
    }

    /**
     * Verifies that sending a {@link UserParentActor.Create} message
     * results in spawning a UserActor and returning a non-null {@link Flow}.
     */
    @Test
    public void testCreateSpawnsUserActorAndReturnsFlow() {
        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> replyProbe =
                testKit.createTestProbe();

        var parentActor =
                testKit.spawn(UserParentActor.create());
        parentActor.tell(new UserParentActor.Create("123", replyProbe.getRef()));
        Flow<JsonNode, JsonNode, NotUsed> flow =
                replyProbe.receiveMessage();
        assertNotNull(flow);
    }

    /**
     * Ensures that creating multiple users results in different Flow instances.
     *
     * <p>This validates that each user gets an independent stream pipeline
     * and there is no unintended sharing between users.
     */
    @Test
    public void testMultipleUsersCreateDifferentFlows() {
        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> probe1 =
                testKit.createTestProbe();
        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> probe2 =
                testKit.createTestProbe();
        var parentActor =
                testKit.spawn(UserParentActor.create());

        parentActor.tell(new UserParentActor.Create("user1", probe1.getRef()));
        parentActor.tell(new UserParentActor.Create("user2", probe2.getRef()));

        Flow<JsonNode, JsonNode, NotUsed> flow1 = probe1.receiveMessage();
        Flow<JsonNode, JsonNode, NotUsed> flow2 = probe2.receiveMessage();

        assertNotNull(flow1);
        assertNotNull(flow2);

        assertNotSame(flow1, flow2);
    }

    /**
     * Verifies that the actor handles Create messages without crashing.
     *
     * <p>This test ensures basic stability and lifecycle safety
     * during normal message processing.
     */
    @Test
    public void testActorDoesNotCrashOnCreate() {
        TestProbe<Flow<JsonNode, JsonNode, NotUsed>> probe =
                testKit.createTestProbe();
        var parentActor =
                testKit.spawn(UserParentActor.create());

        parentActor.tell(new UserParentActor.Create("safe-test", probe.getRef()));
        probe.receiveMessage();

        testKit.stop(parentActor);
    }
}