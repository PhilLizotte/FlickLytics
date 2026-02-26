package models.dto;

import java.util.List;

/**
 * This is DTO representing TMDb TV search response.
 *
 * @author all_team_during_discord_meeting
 */

public class TVShowSearchResponseDTO {
    public int page;
    public List<TVShowDTO> results;
    public int total_pages;
    public int total_results;
}