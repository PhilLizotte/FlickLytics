package models.dto;

import java.util.List;

/**
 * DTO representing the TMDb TV search response.
 * Contains paging info and a list of TV show results.
 *
 * @author all_team_during_discord_meeting
 */
public class TVShowSearchResponseDTO {

    /** Current page number. */
    public int page;

    /** List of TV show results. */
    public List<TVShowDTO> results;

    /** Total number of pages available. */
    public int total_pages;

    /** Total number of results available. */
    public int total_results;
}