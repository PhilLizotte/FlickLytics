package models.dto;

public class PersonKnownForItemDTO {

    private final Integer id;
    private final String mediaType;
    private final String title;
    private final String releaseDate;
    private final Double popularity;
    private final Double voteAverage;
    private final Integer voteCount;
    private final String tmdbUrl;

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

    public Integer getId() {
        return id;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Double getPopularity() {
        return popularity;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public String getTmdbUrl() {
        return tmdbUrl;
    }
}
