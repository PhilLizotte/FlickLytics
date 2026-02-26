package models.domain;

/**
 * This is domain model for Production Company.
 *
 * @author all_team_during_discord_meeting
 */

public class ProductionCompany {
    private final int id;
    private final String name;

    public ProductionCompany(int id, String name) {
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