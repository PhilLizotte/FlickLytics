package services.features;

import static org.junit.Assert.*;
import org.junit.Test;
// import org.junit.jupiter.api.BeforeEach;
import org.junit.Before;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.Application;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import services.features.reviews.ReviewSentimentService;
import models.domain.Review;
import services.tmdb.TmdbConfig;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Craig Kogan (40175780)
 *         Testing class for ReviewSentimentService
 */
public class ReviewSentimentServiceTest extends WithApplication {

    WSClient ws;
    WSRequest request;
    WSResponse response;
    TmdbConfig config;
    ReviewSentimentService rss;
    Review review;

    ArrayList<String> mockReviews = new ArrayList<>();

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    // I couldn't get the @BeforeEach tag to work, so I'm just going to call this at
    // the top of every test.
    // libraryDependencies += "org.junit.jupiter" % "junit-jupiter" % "5.10.0" %
    // Test
    /**
     * @author Craig Kogan (40175780)
     *         Sets up always-needed fields for tests.
     *         This should have the @BeforeEach tag, but I couldn't get that to work
     */
    @Before
    public void setUp() {
        ws = mock(WSClient.class);
        request = mock(WSRequest.class);
        response = mock(WSResponse.class);
        config = mock(TmdbConfig.class);
        rss = new ReviewSentimentService();
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test standard case of review
     *         Input several sample reviews
     *         Ensure that each review is properly scored
     *         Ensure that the overall sentiment is properly calculated
     */
    @Test
    public void testExtractSentimentReviewTest() {
        mockReviews.add("excellent amazing fantastic terrible"); // pos
        mockReviews.add("excellent amazing terrible awful"); // neu
        mockReviews.add(""); // empty (neu)
        mockReviews.add("bad"); // neg
        mockReviews.add("excellent flaw horrible terrible"); // neg
        mockReviews.add("excellent amazing fantastic terrible"); // pos

        String[] expected = { ":-)", ":|", ":|", ":-(", ":-(", ":-)" };

        review = rss.extractSentiment(mockReviews);
        assertEquals("review 0", review.getSentimentAtIndex(0), expected[0]);
        assertEquals("review 1", review.getSentimentAtIndex(1), expected[1]);
        assertEquals("review 2", review.getSentimentAtIndex(2), expected[2]);
        assertEquals("review 3", review.getSentimentAtIndex(3), expected[3]);
        assertEquals("review 4", review.getSentimentAtIndex(4), expected[4]);
        assertEquals("review 5", review.getSentimentAtIndex(5), expected[5]);
        assertEquals("overall sentiment", review.getOverallSentiment(), ":|");
        assertEquals("nb happy", review.getHappy(), 2);
        assertEquals("nb sad", review.getSad(), 2);
        assertEquals("nb neutral", review.getNeutral(), 2);
    }

    /**
     * @author Craig Kogan (40175780)
     */
    @Test
    public void noReviewsReviewTest() {
        review = rss.extractSentiment(mockReviews);
        assertEquals("no reviews", review.getOverallSentiment(), ":|");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test minimum happy threshold
     */
    @Test
    public void minHappyOutcomeReviewTest() {
        // 7 positive and 3 negative reviews.
        // This puts it at the threshold for being overall positive
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");

        review = rss.extractSentiment(mockReviews);
        assertEquals("minimum happy", review.getOverallSentiment(), ":-)");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test under minimum happy threshold
     */
    @Test
    public void underMinHappyOutcomeReviewTest() {
        // 6 positive, 3 negative reviews, 1 neutral.
        // This puts it at just under threshold for being overall positive
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add(" ");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");

        review = rss.extractSentiment(mockReviews);
        assertEquals("under minimum happy", review.getOverallSentiment(), ":|");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test minimum sad threshold
     */
    @Test
    public void minSadOutcomeReviewTest() {
        // 7 negative and 3 positive reviews.
        // This puts it at the threshold for being overall negative
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");

        review = rss.extractSentiment(mockReviews);
        assertEquals("minimum sad", review.getOverallSentiment(), ":-(");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test under minimum sad threshold
     */
    @Test
    public void underMinSadOutcomeReviewTest() {
        // 6 negative, 3 positive, 1 neutral reviews.
        // This puts it at just under the threshold for being overall negative
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add("bad");
        mockReviews.add(" ");
        mockReviews.add("fun");
        mockReviews.add("fun");
        mockReviews.add("fun");

        review = rss.extractSentiment(mockReviews);
        assertEquals("under minimum sad", review.getOverallSentiment(), ":|");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that all neutral reviews results in a neutral overall result
     */
    @Test
    public void allNeutralReviewTest() {
        mockReviews.add("bad fun"); // expected to be neutral
        mockReviews.add(" "); // expected to be neutral

        review = rss.extractSentiment(mockReviews);
        // Assert that each individual is neutral
        assertEquals("all neutral review 1", review.getSentimentAtIndex(0), ":|");
        assertEquals("all neutral review 2", review.getSentimentAtIndex(1), ":|");

        // Assert that overall is neutral
        assertEquals("all neutral overall", review.getOverallSentiment(), ":|");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that a single neutral review results in a neutral overall result
     */
    @Test
    public void singleNeutralReviewTest() {
        mockReviews.add("bad fun"); // expected to be neutral

        review = rss.extractSentiment(mockReviews);
        // Assert that the single individual is neutral
        assertEquals("single neutral review", review.getSentimentAtIndex(0), ":|");

        // Assert that overall is neutral
        assertEquals("single neutral overall", review.getOverallSentiment(), ":|");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that a single happy review results in a happy overall result
     */
    @Test
    public void singleHappyReviewTest() {
        mockReviews.add("fun"); // expected to be happy

        review = rss.extractSentiment(mockReviews);
        // Assert that the single individual is happy
        assertEquals("single happy review", review.getSentimentAtIndex(0), ":-)");

        // Assert that overall is happy
        assertEquals("single happy overall", review.getOverallSentiment(), ":-)");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that a single sad review results in a sad overall result
     */
    @Test
    public void singleSadReviewTest() {
        mockReviews.add("bad"); // expected to be sad

        review = rss.extractSentiment(mockReviews);
        // Assert that the single individual is sad
        assertEquals("single sad review", review.getSentimentAtIndex(0), ":-(");

        // Assert that overall is sad
        assertEquals("single sad overall", review.getOverallSentiment(), ":-(");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that additional whitespace does not matter
     */
    @Test
    public void whiteSpaceReviewTest() {
        mockReviews.add("bad         fun      fun         bad"); // expected to be neutral

        review = rss.extractSentiment(mockReviews);
        assertEquals("white space specific review", review.getSentimentAtIndex(0), ":|");
        assertEquals("white space overall", review.getOverallSentiment(), ":|");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that special characters and numbers do not matter,
     *         regardless of if it's touching words or not, in terms of the
     *         sentiment calculation.
     * 
     *         Also verify that these special characters are not destroyed
     */
    @Test
    public void spacialCharactersReviewTest() {
        mockReviews.add("bad $%^&fun      fun   $%^& 4564156489      bad"); // expected to be neutral

        review = rss.extractSentiment(mockReviews);
        assertEquals("special chars specific review", review.getSentimentAtIndex(0), ":|");
        assertEquals("special chars preservation", review.getReviews().get(0),
                "bad $%^&fun      fun   $%^& 4564156489      bad");
        assertEquals("special chars overall", review.getOverallSentiment(), ":|");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that negations work properly on sad words
     */
    @Test
    public void negativeNegationsReviewTest() {
        mockReviews.add("not bad"); // expected to be positive

        review = rss.extractSentiment(mockReviews);
        assertEquals("neg negation specific review", review.getSentimentAtIndex(0), ":-)");
        assertEquals("neg negation overall", review.getOverallSentiment(), ":-)");
    }

    /**
     * @author Craig Kogan (40175780)
     *         Test that negations work properly on happy words
     */
    @Test
    public void positiveNegationsReviewTest() {
        mockReviews.add("not good"); // expected to be negative

        review = rss.extractSentiment(mockReviews);
        assertEquals("pos negation specific review", review.getSentimentAtIndex(0), ":-(");
        assertEquals("pos negation overall", review.getOverallSentiment(), ":-(");
    }
}
