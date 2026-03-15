package models.dto;

import java.util.List;

/**
 * DTO representing the TMDb movie search response.
 * Contains paging info and a list of movie results.
 *
 * @author all_team_during_discord_meeting
 */
public class MovieSearchResponseDTO {

    /** Current page number. */
    public int page;

    /** List of movie results. */
    public List<MovieDTO> results;

    /** Total number of pages available. */
    public int total_pages;

    /** Total number of results available. */
    public int total_results;
}