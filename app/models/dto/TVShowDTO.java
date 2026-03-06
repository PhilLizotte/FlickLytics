package models.dto;

import models.domain.Creator;
import models.domain.Genre;
import models.domain.Network;

import java.time.LocalDate;
import java.util.List;

/**
 * This is DTO representing a TV show returned by TMDb API.
 *
 * @author all_team_during_discord_meeting
 */

public class TVShowDTO {
    public boolean adult;
    public String backdrop_path;
    public List<Creator> created_by;
    public int episode_run_time;
    public LocalDate first_air_date;
    public List<Genre> genres;
    public String homepage;
    public int id;
    public boolean in_production;
    public List<String> languages;
    public LocalDate last_air_date;
    public List<Network> networks;
    public int number_of_episodes;
    public int number_of_seasons;
    public String status;
    public String tagline;
    public String type;
    public List<String> origin_country;
    public String original_language;
    public String original_name;
    public String overview;
    public double popularity;
    public String poster_path;
    public String name;
    public double vote_average;
    public int vote_count;
}