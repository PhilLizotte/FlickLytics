package models.domain;

/**
 * This is domain model for Genre.
 *
 * @author all_team_during_discord_meeting
 */

public class Genre {
    private final int id;
    private final String name;
    
    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}
