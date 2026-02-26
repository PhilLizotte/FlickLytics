package models.domain;

/**
 * This is domain model representing a single search result item.
 *
 * @author all_team_during_discord_meeting
 */

public class SearchResult {
    private final int id;
    private final String title;
    private final String mediaType;
    private final String language;
    private final String releaseDate;
    private final double popularity;
    private final double voteAverage;
    private final String imagePath;

    /**
     * Constructs a SearchResult object.
     *
     * @param id TMDb ID
     * @param title title or name
     * @param mediaType movie, tv, or person
     * @param language original language
     * @param releaseDate release or air date
     * @param popularity popularity score
     * @param voteAverage vote average score
     * @param imagePath poster or profile path
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


    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getLanguage() {
        return language;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public double getPopularity() {
        return popularity;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getImagePath() {
        return imagePath;
    }
}