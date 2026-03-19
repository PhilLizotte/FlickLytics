package models.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain model representing a TV show.
 * Contains all relevant information based on the project document appendix.
 *
 * @author all_team_during_discord_meeting
 */
public class TVShow {

    /** Unique identifier of the TV show. */
    private final int id;

    /** Title of the TV show. */
    private final String name;

    /** Brief description of the TV show. */
    private final String overview;

    /** First air date of the TV show. */
    private final LocalDate firstAirDate;

    /** Last air date of the TV show. */
    private final LocalDate lastAirDate;

    /** Popularity score of the TV show. */
    private final double popularity;

    /** Path to the poster image. */
    private final String posterPath;

    /** List of genres associated with the TV show. */
    private final List<Genre> genres;

    /** Official homepage URL. */
    private final String homepage;

    /** Total number of episodes. */
    private final int numberOfEpisodes;

    /** Total number of seasons. */
    private final int numberOfSeasons;

    /** Networks that broadcast the TV show. */
    private final List<Network> networks;

    /** Current status of the TV show (e.g., Running, Ended). */
    private final String status;

    /** Promotional tagline of the TV show. */
    private final String tagline;

    /** Average user rating. */
    private final double voteAverage;

    /** Number of votes. */
    private final int voteCount;

    /** Type of TV show (e.g., Scripted, Reality). */
    private final String type;

    /**
     * Creates a new {@code TVShow} instance with all attributes.
     *
     * @param id unique identifier
     * @param name title
     * @param overview brief description
     * @param firstAirDate first air date
     * @param lastAirDate last air date
     * @param popularity popularity score
     * @param posterPath poster image path
     * @param genres list of genres
     * @param homepage official homepage URL
     * @param numberOfEpisodes total episodes
     * @param numberOfSeasons total seasons
     * @param networks networks that broadcast the show
     * @param status current status
     * @param tagline promotional tagline
     * @param voteAverage average user rating
     * @param voteCount number of votes
     * @param type show type
     */
    public TVShow(int id, String name, String overview, LocalDate firstAirDate,
                  LocalDate lastAirDate, double popularity, String posterPath,
                  List<Genre> genres, String homepage, int numberOfEpisodes,
                  int numberOfSeasons, List<Network> networks, String status,
                  String tagline, double voteAverage, int voteCount, String type) {
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

    /**
     * Returns the TV show ID.
     * @return the ID
     */
    public int getId() { return id; }

    /**
     * Returns the TV show title.
     * @return the title
     */
    public String getName() { return name; }

    /**
     * Returns the TV show overview.
     * @return the overview
     */
    public String getOverview() { return overview; }

    /**
     * Returns the first air date.
     * @return the first air date
     */
    public LocalDate getFirstAirDate() { return firstAirDate; }

    /**
     * Returns the last air date.
     * @return the last air date
     */
    public LocalDate getLastAirDate() { return lastAirDate; }

    /**
     * Returns the popularity score.
     * @return the popularity
     */
    public double getPopularity() { return popularity; }

    /**
     * Returns the poster image path.
     * @return the poster path
     */
    public String getPosterPath() { return posterPath; }

    /**
     * Returns the list of genres.
     * @return the genres
     */
    public List<Genre> getGenres() { return genres; }

    /**
     * Returns the official homepage URL.
     * @return the homepage URL
     */
    public String getHomepage() { return homepage; }

    /**
     * Returns the total number of episodes.
     * @return the number of episodes
     */
    public int getNumberOfEpisodes() { return numberOfEpisodes; }

    /**
     * Returns the total number of seasons.
     * @return the number of seasons
     */
    public int getNumberOfSeasons() { return numberOfSeasons; }

    /**
     * Returns the networks that broadcast the show.
     * @return the networks
     */
    public List<Network> getNetworks() { return networks; }

    /**
     * Returns the current status of the show.
     * @return the status
     */
    public String getStatus() { return status; }

    /**
     * Returns the promotional tagline.
     * @return the tagline
     */
    public String getTagline() { return tagline; }

    /**
     * Returns the average user rating.
     * @return the vote average
     */
    public double getVoteAverage() { return voteAverage; }

    /**
     * Returns the number of votes.
     * @return the vote count
     */
    public int getVoteCount() { return voteCount; }

    /**
     * Returns the TV show type.
     * @return the type
     */
    public String getType() { return type; }
}