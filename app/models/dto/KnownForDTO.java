package models.dto;

import java.util.List;

/**
 * This is DTO representing media that a person is known for.
 *
 * @author all_team_during_discord_meeting
 */

public class KnownForDTO {
    public boolean adult;
    public String backdrop_path;
    public int id;
    public String title;
    public String original_title;
    public String overview;
    public String poster_path;
    public String media_type;
    public List<Integer> genre_ids;
    public double popularity;
    public String release_date;
    public boolean video;
    public double vote_average;
    public int vote_count;
}