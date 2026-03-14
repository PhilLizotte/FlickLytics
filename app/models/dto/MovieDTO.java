package models.dto;

import models.domain.Genre;
import models.domain.ProductionCompany;
import models.domain.SpokenLanguage;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing a Movie returned by TMDb API.
 * Contains detailed information about the movie.
 *
 * @author all_team_during_discord_meeting
 */
public class MovieDTO {

    /** Indicates whether the movie is adult content. */
    public boolean adult;

    /** Path to the backdrop image. */
    public String backdrop_path;

    /** Budget of the movie in USD. */
    public long budget;

    /** List of genres associated with the movie. */
    public List<Genre> genres;

    /** Official homepage URL of the movie. */
    public String homepage;

    /** Unique identifier of the movie. */
    public int id;

    /** IMDb ID of the movie. */
    public String imdb_id;

    /** Original language of the movie. */
    public String original_language;

    /** Original title of the movie. */
    public String original_title;

    /** Overview or description of the movie. */
    public String overview;

    /** Popularity score of the movie. */
    public double popularity;

    /** Path to the poster image. */
    public String poster_path;

    /** List of production companies associated with the movie. */
    public List<ProductionCompany> production_companies;

    /** Release date of the movie. */
    public LocalDate release_date;

    /** Revenue of the movie in USD. */
    public long revenue;

    /** Runtime of the movie in minutes. */
    public int runtime;

    /** List of spoken languages in the movie. */
    public List<SpokenLanguage> spoken_languages;

    /** Current status of the movie (e.g., Released, Post Production). */
    public String status;

    /** Promotional tagline of the movie. */
    public String tagline;

    /** Title of the movie. */
    public String title;

    /** Indicates whether a video is associated with the movie. */
    public boolean video;

    /** Average user rating of the movie. */
    public double vote_average;

    /** Total number of votes. */
    public int vote_count;
}