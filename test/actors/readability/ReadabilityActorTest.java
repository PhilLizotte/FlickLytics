package actors.readability;

import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

import services.features.readability.ReadabilityService;

/**
 * Unit test class for {@link ReadabilityActor}.
 *
 * <p>This test verifies the correct behavior of the ReadabilityActor in response
 * to incoming Compute messages. It ensures that the actor properly delegates
 * readability calculations to the {@link ReadabilityService} and returns the
 * expected results.</p>
 *
 * <p>The test uses {@link TestKitJunitResource} from Pekko Typed TestKit to
 * provide an actor system environment and {@link TestProbe} to capture and
 * assert actor responses.</p>
 *
 * <p>Test coverage includes:</p>
 * <ul>
 *     <li>Actor creation and initialization</li>
 *     <li>Handling of Compute messages</li>
 *     <li>Correct calculation of Flesch Reading Ease score</li>
 *     <li>Correct calculation of Flesch-Kincaid Grade Level</li>
 *     <li>Proper asynchronous response delivery via TestProbe</li>
 * </ul>
 *
 * @author Ali Maher
 */
public class ReadabilityActorTest {

    /**
     * Shared Pekko TestKit resource for managing the actor system lifecycle.
     * This is initialized once for all tests in this class.
     */
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
    static ReadabilityService readabilityService;

    /**
     * Initializes shared resources before any tests are executed.
     * Creates an instance of {@link ReadabilityService}.
     */
    @BeforeClass
    public static void setup() {
        readabilityService = new ReadabilityService();  
    }

    /**
     * Tests that the ReadabilityActor correctly processes a Compute message.
     *
     * <p>Test flow:</p>
     * <ol>
     *     <li>Create a {@link TestProbe} to receive actor responses</li>
     *     <li>Spawn a new instance of {@link ReadabilityActor}</li>
     *     <li>Send a Compute message with sample input text</li>
     *     <li>Receive the result from the probe</li>
     *     <li>Verify that the computed readability scores match expected values</li>
     * </ol>
     *
     * <p>The assertions compare the actor's output with direct calculations
     * from {@link ReadabilityService}, using a delta to account for floating-point precision.</p>
     */
    @Test
    public void testComputeMessage() {
        TestProbe<ReadabilityActor.Result> probe = testKit.createTestProbe();

        var readabilityActor = testKit.spawn(ReadabilityActor.create(readabilityService));
        readabilityActor.tell(new ReadabilityActor.Compute("Hello world", probe.getRef()));
        ReadabilityActor.Result result = probe.receiveMessage();

        // assertions
        assertEquals(readabilityService.calculateFleschReaddingEase("Hello world"), result.fleschScore, 0.001);
        assertEquals(readabilityService.calculateFleschKincaidGradeLevel("Hello world"), result.gradeLevel, 0.001);
    }
}