package models.dto;

/**
 * DTO representing summary statistics for a collection of numeric values.
 * Used by the person known-for feature to summarize popularity, vote average,
 * and vote count metrics.
 *
 * @author Aram Zand
 */
public class PersonKnownForStatsDTO {

    /** Number of values included in the summary. */
    private final long count;

    /** Minimum value in the summary. */
    private final Double min;

    /** Maximum value in the summary. */
    private final Double max;

    /** Average value in the summary. */
    private final Double average;

    /**Constructs a summary statistics DTO. */
    public PersonKnownForStatsDTO(long count, Double min, Double max, Double average) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.average = average;
    }

    /** Returns the number of values included in the summary. */
    public long getCount() {
        return count;
    }

    /** Returns the minimum value. */
    public Double getMin() {
        return min;
    }

    /** Returns the maximum value. */
    public Double getMax() {
        return max;
    }

    /** Returns the average value. */
    public Double getAverage() {
        return average;
    }
}
