package actors.reviews;

import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.junit.ClassRule;
import org.junit.Test;

import models.domain.Review;

import org.junit.BeforeClass;
import static org.junit.Assert.*;

import java.util.Arrays;

import services.features.reviews.ReviewSentimentService;

/**
 * Unit tests for the ReviewActor.
 * 
 * @author Craig Kogan
 */
public class ReviewActorTest {

    /**
     * Shared Pekko TestKit resource for managing the actor system lifecycle.
     * This is initialized once for all tests in this class.
     */
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
    static ReviewSentimentService rss;

    @BeforeClass
    public static void setUp() {
        rss = new ReviewSentimentService();
    }

    @Test
    public void testComputeMessageReview() {
        TestProbe<ReviewActor.Result> probe = testKit.createTestProbe();

        var reviewActor = testKit.spawn(ReviewActor.create(rss));
        reviewActor.tell(new ReviewActor.Compute(
                Arrays.asList("Some review", "I would really love to eat some bread right about now..."),
                probe.getRef()));
        Review review = probe.receiveMessage().review;

        assertEquals("review actor test", review.getSentimentAtIndex(1), ":-)");
    }

}
