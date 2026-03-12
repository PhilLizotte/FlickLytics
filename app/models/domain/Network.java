package models.domain;

/**
 * This is domain model for Network.
 *
 * @author all_team_during_discord_meeting
 */

public class Network {

    private final int id;
    private final String logoPath;
    private final String name;
    private final String origin_country;

     public Network(int id, String logoPath, String name, String origin_country) {
        this.id = id;
        this.logoPath = logoPath;
        this.name = name;
        this.origin_country = origin_country;
    }

    public int getId() {
        return id;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getName() {
        return name;
    }

    public String getOrigin_country() {
        return origin_country;
    }
}