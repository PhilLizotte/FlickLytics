package models.dto;

import models.domain.Creator;
import models.domain.Genre;
import models.domain.Network;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing a TV show returned by TMDb API.
 * Contains detailed information about the show.
 *
 * @author all_team_during_discord_meeting
 */
public class TVShowDTO {

    /** Indicates whether the show is adult content. */
    public boolean adult;

    /** Path to the backdrop image. */
    public String backdrop_path;

    /** List of creators of the show. */
    public List<Creator> created_by;

    /** Array of episode run times in minutes. */
    public int[] episode_run_time;

    /** First air date of the show. */
    public LocalDate first_air_date;

    /** List of genres associated with the show. */
    public List<Genre> genres;

    /** Official homepage URL. */
    public String homepage;

    /** Unique identifier of the show. */
    public int id;

    /** Indicates if the show is currently in production. */
    public boolean in_production;

    /** List of languages used in the show. */
    public List<String> languages;

    /** Last air date of the show. */
    public LocalDate last_air_date;

    /** Networks that broadcast the show. */
    public List<Network> networks;

    /** Total number of episodes. */
    public int number_of_episodes;

    /** Total number of seasons. */
    public int number_of_seasons;

    /** Current status of the show (e.g., Running, Ended). */
    public String status;

    /** Promotional tagline of the show. */
    public String tagline;

    /** Type of the show (e.g., Scripted, Reality). */
    public String type;

    /** List of origin countries. */
    public List<String> origin_country;

    /** Original language of the show. */
    public String original_language;

    /** Original name of the show. */
    public String original_name;

    /** Overview or description of the show. */
    public String overview;

    /** Popularity score. */
    public double popularity;

    /** Path to the poster image. */
    public String poster_path;

    /** Title of the show. */
    public String name;

    /** Average user rating. */
    public double vote_average;

    /** Total number of votes. */
    public int vote_count;
}