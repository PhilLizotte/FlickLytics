package models.dto;

import java.util.List;

/**
 * DTO representing the TMDb person search response.
 * Contains paging info and a list of person results.
 *
 * @author all_team_during_discord_meeting
 */
public class PersonSearchResponseDTO {

    /** Current page number. */
    public int page;

    /** List of person results. */
    public List<PersonDTO> results;

    /** Total number of pages available. */
    public int total_pages;

    /** Total number of results available. */
    public int total_results;
}