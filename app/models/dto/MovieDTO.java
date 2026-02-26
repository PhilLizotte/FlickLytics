package models.dto;

import java.util.List;

/**
 * This is DTO representing a Movie returned by TMDb API.
 *
 * @author all_team_during_discord_meeting
 */

public class MovieDTO {
    public boolean adult;
    public String backdrop_path;
    public List<Integer> genre_ids;
    public int id;
    public String original_language;
    public String original_title;
    public String overview;
    public double popularity;
    public String poster_path;
    public String release_date;
    public String title;
    public boolean video;
    public double vote_average;
    public int vote_count;
}