package models.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Craig Kogan (40175780)
 *         Test the Review object - the cosntructor and all the getters.
 */
public class ReviewTest {

    /**
     * Test the getters
     */
    @Test
    public void testReviewClass() {

        List<String> reviews = Arrays.asList("fun", "fun", "fun", "fun", "fun", "fun", "fun", "bad", "bad", "");
        List<String> sentiments = Arrays.asList(":-)", ":-)", ":-)", ":-)", ":-)", ":-)", ":-)", ":-(", ":-(", ":|");
        Review review = new Review(7, 2, 1, ":-)",
                reviews, sentiments);

        // assertEquals("getid", review.getId(), 1);
        assertEquals("getHappy", review.getHappy(), 7);
        assertEquals("getSad", review.getSad(), 2);
        assertEquals("getNeutral", review.getNeutral(), 1);
        assertEquals("getTotal", review.getTotal(), 10);
        assertEquals("getOverallSentiment", review.getOverallSentiment(), ":-)");
        assertEquals("getReviews", review.getReviews(), reviews);
        assertEquals("getSentiments", review.getSentiments(), sentiments);
        // I'm only doing this for the coverage. This method is unsafe. Showing that it
        // is unsafe by forcing it to throw an error is not needed.
        // It is unsafe by design.
        assertEquals("getSentimentAtIndex", review.getSentimentAtIndex(0), ":-)");
    }

}
