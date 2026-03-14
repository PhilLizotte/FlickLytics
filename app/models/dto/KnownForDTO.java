package models.dto;

import java.util.List;

/**
 * DTO representing media that a person is known for (movie or TV show).
 * Contains basic information about the media item.
 *
 * @author all_team_during_discord_meeting
 */
public class KnownForDTO {

    /** Indicates whether the media is adult content. */
    public boolean adult;

    /** Path to the backdrop image. */
    public String backdrop_path;

    /** Unique identifier of the media. */
    public int id;

    /** Title of the media. */
    public String title;

    /** Original title of the media. */
    public String original_title;

    /** Overview or description of the media. */
    public String overview;

    /** Path to the poster image. */
    public String poster_path;

    /** Type of media (e.g., movie, tv). */
    public String media_type;

    /** List of genre IDs associated with the media. */
    public List<Integer> genre_ids;

    /** Popularity score of the media. */
    public double popularity;

    /** Release date as a string (format: YYYY-MM-DD). */
    public String release_date;

    /** Indicates whether a video is associated with the media. */
    public boolean video;

    /** Average user rating of the media. */
    public double vote_average;

    /** Total number of votes. */
    public int vote_count;
}