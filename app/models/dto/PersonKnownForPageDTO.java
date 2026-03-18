package models.dto;

import java.util.List;

/**
 * DTO representing the known-for page data for a person.
 * Contains a list of known-for items along with summary statistics for
 * popularity, vote average, and vote count.
 *
 * @author Aram Zand
 */
public class PersonKnownForPageDTO {

    /** List of known-for items. */
    private final List<PersonKnownForItemDTO> items;

    /** Summary statistics for popularity. */
    private final PersonKnownForStatsDTO popularityStats;

    /** Summary statistics for vote average. */
    private final PersonKnownForStatsDTO voteAverageStats;

    /** Summary statistics for vote count. */
    private final PersonKnownForStatsDTO voteCountStats;

    /** Constructs the known-for page DTO. */
    public PersonKnownForPageDTO(List<PersonKnownForItemDTO> items,
                                PersonKnownForStatsDTO popularityStats,
                                PersonKnownForStatsDTO voteAverageStats,
                                PersonKnownForStatsDTO voteCountStats) {
        this.items = items;
        this.popularityStats = popularityStats;
        this.voteAverageStats = voteAverageStats;
        this.voteCountStats = voteCountStats;
    }

    /** Returns the known-for items. */
    public List<PersonKnownForItemDTO> getItems() {
        return items;
    }

    /** Returns the popularity summary statistics. */
    public PersonKnownForStatsDTO getPopularityStats() {
        return popularityStats;
    }

    /** Returns the vote average summary statistics. */
    public PersonKnownForStatsDTO getVoteAverageStats() {
        return voteAverageStats;
    }

    /** Returns the vote count summary statistics. */
    public PersonKnownForStatsDTO getVoteCountStats() {
        return voteCountStats;
    }
}
