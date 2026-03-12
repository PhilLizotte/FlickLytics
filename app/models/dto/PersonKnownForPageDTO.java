package models.dto;

import java.util.List;

public class PersonKnownForPageDTO {

    private final List<PersonKnownForItemDTO> items;
    private final PersonKnownForStatsDTO popularityStats;
    private final PersonKnownForStatsDTO voteAverageStats;
    private final PersonKnownForStatsDTO voteCountStats;

    public PersonKnownForPageDTO(List<PersonKnownForItemDTO> items,
                                PersonKnownForStatsDTO popularityStats,
                                PersonKnownForStatsDTO voteAverageStats,
                                PersonKnownForStatsDTO voteCountStats) {
        this.items = items;
        this.popularityStats = popularityStats;
        this.voteAverageStats = voteAverageStats;
        this.voteCountStats = voteCountStats;
    }

    public List<PersonKnownForItemDTO> getItems() {
        return items;
    }

    public PersonKnownForStatsDTO getPopularityStats() {
        return popularityStats;
    }

    public PersonKnownForStatsDTO getVoteAverageStats() {
        return voteAverageStats;
    }

    public PersonKnownForStatsDTO getVoteCountStats() {
        return voteCountStats;
    }
}
