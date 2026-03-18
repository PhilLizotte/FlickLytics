package models.dto;

/**
 * DTO representing a single item in a person's "known-for" list.
 * Contains the media type, title, release date, popularity, voting metrics,
 * and a TMDb URL for the item.
 *
 * @author Aram Zand
 */
public class PersonKnownForItemDTO {

    /** Unique identifier of the media item. */
    private final Integer id;

    /** Type of media returned by TMDb (movie, tv). */
    private final String mediaType;

    /** Display title/name of the media item. */
    private final String title;

    /** Release date (movie) or first air date (tv). */
    private final String releaseDate;

    /** Popularity score from TMDb. */
    private final Double popularity;

    /** Vote average from TMDb. */
    private final Double voteAverage;

    /** Vote count from TMDb. */
    private final Integer voteCount;

    /** Link to the TMDb details page for this item. */
    private final String tmdbUrl;

    /** Constructs a known-for item DTO. */
    public PersonKnownForItemDTO(Integer id,
                                String mediaType,
                                String title,
                                String releaseDate,
                                Double popularity,
                                Double voteAverage,
                                Integer voteCount,
                                String tmdbUrl) {
        this.id = id;
        this.mediaType = mediaType;
        this.title = title;
        this.releaseDate = releaseDate;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.tmdbUrl = tmdbUrl;
    }

    /** Returns the unique identifier of the media item. */
    public Integer getId() {
        return id;
    }

    /** Returns the type of media (movie or tv). */
    public String getMediaType() {
        return mediaType;
    }

    /** Returns the title/name of the media item. */
    public String getTitle() {
        return title;
    }

    /** Returns the release date/first air date. */
    public String getReleaseDate() {
        return releaseDate;
    }

    /** Returns the popularity score. */
    public Double getPopularity() {
        return popularity;
    }

    /** Returns the vote average. */
    public Double getVoteAverage() {
        return voteAverage;
    }

    /** Returns the vote count. */
    public Integer getVoteCount() {
        return voteCount;
    }

    /** Returns the TMDb URL for the item. */
    public String getTmdbUrl() {
        return tmdbUrl;
    }
}
