package models.dto;

import java.util.List;

/**
 * This is DTO representing TMDb person search response.
 *
 * @author all_team_during_discord_meeting
 */

public class PersonSearchResponseDTO {
    public int page;
    public List<PersonDTO> results;
    public int total_pages;
    public int total_results;
}