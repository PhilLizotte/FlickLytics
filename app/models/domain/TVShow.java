package models.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * This is domain model for representing TVShow.
 * The fields are based on the Appendix of the Project Document.
 *
 * @author  all_team_during_discord_meeting
 */

public class TVShow {
    private final int id;
    private final String name;
    private final String overview;
    private final LocalDate firstAirDate;
    private final LocalDate lastAirDate;
    private final double popularity;
    private final String posterPath;
    private final List<Genre> genres;
    private final String homepage;
    private final int numberOfEpisodes;
    private final int numberOfSeasons;
    private final List<Network> networks;
    private final String status;
    private final String tagline;
    private final double voteAverage;
    private final int voteCount;
    private final String type;

    public TVShow(
            int id,
            String name,
            String overview,
            LocalDate firstAirDate,
            LocalDate lastAirDate,
            double popularity,
            String posterPath,
            List<Genre> genres,
            String homepage,
            int numberOfEpisodes,
            int numberOfSeasons,
            List<Network> networks,
            String status,
            String tagline,
            double voteAverage,
            int voteCount,
            String type
    ) {
        this.id = id;
        this.name = name;
        this.overview = overview;
        this.firstAirDate = firstAirDate;
        this.lastAirDate = lastAirDate;
        this.popularity = popularity;
        this.posterPath = posterPath;
        this.genres = genres;
        this.homepage = homepage;
        this.numberOfEpisodes = numberOfEpisodes;
        this.numberOfSeasons = numberOfSeasons;
        this.networks = networks;
        this.status = status;
        this.tagline = tagline;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOverview() {
        return overview;
    }

    public LocalDate getFirstAirDate() {
        return firstAirDate;
    }

    public LocalDate getLastAirDate() {
        return lastAirDate;
    }

    public double getPopularity() {
        return popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public String getHomepage() {
        return homepage;
    }

    public int getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public List<Network> getNetworks() {
        return networks;
    }

    public String getStatus() {
        return status;
    }

    public String getTagline() {
        return tagline;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public String getType() {
        return type;
    }
}
