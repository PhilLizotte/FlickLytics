package models.dto;

import java.util.List;

/**
 * This is DTO representing a Person returned by TMDb API.
 *
 * @author all_team_during_discord_meeting
 */

public class PersonDTO {
    public boolean adult;
    public int gender;
    public int id;
    public String known_for_department;
    public String name;
    public String original_name;
    public double popularity;
    public String profile_path;
    public List<KnownForDTO> known_for;
}