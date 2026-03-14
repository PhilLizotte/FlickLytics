package models.domain;

/**
 * Domain model representing a single search result item returned from TMDb.
 * <p>
 * This class stores the basic information needed to display a search result,
 * such as its identifier, title, media type, language, release date,
 * popularity score, rating, and associated image.
 *
 * @author all_team_during_discord_meeting
 */
public class SearchResult {

    /** Unique identifier of the media item in TMDb. */
    private final int id;

    /** Title or name of the media item. */
    private final String title;

    /** Type of media (e.g., movie, tv, or person). */
    private final String mediaType;

    /** Original language of the media item. */
    private final String language;

    /** Release date or first air date of the media item. */
    private final String releaseDate;

    /** Popularity score of the media item. */
    private final double popularity;

    /** Average vote rating of the media item. */
    private final double voteAverage;

    /** Path to the poster or profile image. */
    private final String imagePath;

    /**
     * Constructs a {@code SearchResult} object.
     *
     * @param id the TMDb identifier of the media item
     * @param title the title or name of the media item
     * @param mediaType the type of media (movie, tv, or person)
     * @param language the original language of the media item
     * @param releaseDate the release or first air date
     * @param popularity the popularity score
     * @param voteAverage the average vote rating
     * @param imagePath the path to the poster or profile image
     */
    public SearchResult(
            int id,
            String title,
            String mediaType,
            String language,
            String releaseDate,
            double popularity,
            double voteAverage,
            String imagePath
    ) {
        this.id = id;
        this.title = title;
        this.mediaType = mediaType;
        this.language = language;
        this.releaseDate = releaseDate;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.imagePath = imagePath;
    }

    /**
     * Returns the TMDb identifier of the media item.
     *
     * @return the item ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the title or name of the media item.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the type of media.
     *
     * @return the media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Returns the original language of the media item.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the release or first air date of the media item.
     *
     * @return the release date
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Returns the popularity score of the media item.
     *
     * @return the popularity score
     */
    public double getPopularity() {
        return popularity;
    }

    /**
     * Returns the average vote rating of the media item.
     *
     * @return the vote average
     */
    public double getVoteAverage() {
        return voteAverage;
    }

    /**
     * Returns the path to the poster or profile image.
     *
     * @return the image path
     */
    public String getImagePath() {
        return imagePath;
    }
}