package models.dto;

import java.util.List;

/**
 * This is DTO representing a TV show returned by TMDb API.
 *
 * @author all_team_during_discord_meeting
 */

public class TVShowDTO {
    public boolean adult;
    public String backdrop_path;
    public List<Integer> genre_ids;
    public int id;
    public List<String> origin_country;
    public String original_language;
    public String original_name;
    public String overview;
    public double popularity;
    public String poster_path;
    public String first_air_date;
    public String name;
    public double vote_average;
    public int vote_count;
}