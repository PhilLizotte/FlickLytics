package models.dto;

import java.util.List;

/**
 * DTO representing a Person returned by TMDb API.
 * Contains detailed information about the person.
 *
 * @author all_team_during_discord_meeting
 */
public class PersonDTO {

    /** Indicates whether the person is adult. */
    public boolean adult;

    /** Gender of the person (typically 1 = female, 2 = male, 0 = unknown). */
    public int gender;

    /** Unique identifier of the person. */
    public int id;

    /** Known department (e.g., Acting, Directing). */
    public String known_for_department;

    /** Name of the person. */
    public String name;

    /** Original name of the person. */
    public String original_name;

    /** Popularity score of the person. */
    public double popularity;

    /** Path to the profile image. */
    public String profile_path;

    /** List of works the person is known for. */
    public List<KnownForDTO> known_for;
}