package models.domain;

/**
 * This is domain model for Network.
 *
 * @author all_team_during_discord_meeting
 */

public class Network {

    private final int id;
    private final String name;

    public Network(int id, String name) {
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