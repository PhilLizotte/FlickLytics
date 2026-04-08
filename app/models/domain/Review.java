package models.domain;

import java.util.List;

/**
 * @author Craig Kogan (40175780)
 *         Domain model to represent the reviews object. Used in the Reviews
 *         page.
 */
public class Review {
    // private final int id;
    private final int happy;
    private final int sad;
    private final int neutral;
    private final int total;
    private final String overallSentiment;
    private final List<String> reviews;
    private final List<String> sentiments;

    /**
     * Constructor without total number of reviews. Total number is calculated as
     * 'happy + sad + neutral'
     * 
     * @param id               show/tvshow id
     * @param happy            number of happy reviews
     * @param sad              number of sad reviews
     * @param neutral          number of neutral reviews
     * @param overallSentiment overall review sentiment
     * @param reviews          list of all the reviews
     * @param sentiments       list of all the sentiments (one sentiment per review)
     */
    public Review(int happy, int sad, int neutral, String overallSentiment, List<String> reviews,
            List<String> sentiments) {
        // this.id = id;
        this.happy = happy;
        this.sad = sad;
        this.neutral = neutral;
        this.total = happy + sad + neutral;
        this.overallSentiment = overallSentiment;
        this.reviews = reviews;
        this.sentiments = sentiments;
    }

    // /**
    // * @return movie/show id
    // */
    // public int getId() {
    // return id;
    // }

    /**
     * @return number of happy reviews
     */
    public int getHappy() {
        return happy;
    }

    /**
     * @return number of sad reviews
     */
    public int getSad() {
        return sad;
    }

    /**
     * @return number of neutral reviews
     */
    public int getNeutral() {
        return neutral;
    }

    /**
     * @return total number of reviews
     */
    public int getTotal() {
        return total;
    }

    /**
     * @return overall sentiment
     */
    public String getOverallSentiment() {
        return overallSentiment;
    }

    /**
     * @return all the reviews
     */
    public List<String> getReviews() {
        return reviews;
    }

    /**
     * @return all the sentiments. One sentiment per review
     */
    public List<String> getSentiments() {
        return sentiments;
    }

    /**
     * This method is only used to display the sentiments alongside the reviews. It
     * should only ever be called from the review page.
     * This method is unsafe! There are no checks being done to ensure the index is
     * within the bounds of the list!
     * 
     * @param index index in the list
     * @return A sentiment at a specific index.
     */
    public String getSentimentAtIndex(int index) {
        return sentiments.get(index);
    }
}
