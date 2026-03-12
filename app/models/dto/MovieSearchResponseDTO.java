package models.dto;

import java.util.List;

/**
 * This is DTO representing TMDb movie search response.
 *
 * @author all_team_during_discord_meeting
 */

public class MovieSearchResponseDTO {
    public int page;
    public List<MovieDTO> results;
    public int total_pages;
    public int total_results;
}