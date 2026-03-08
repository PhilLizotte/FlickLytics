package models.dto;

public class PersonKnownForStatsDTO {

    private final long count;
    private final Double min;
    private final Double max;
    private final Double average;

    public PersonKnownForStatsDTO(long count, Double min, Double max, Double average) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.average = average;
    }

    public long getCount() {
        return count;
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }

    public Double getAverage() {
        return average;
    }
}
