package models.domain;

/**
 * This is domain model for Production Company.
 *
 * @author all_team_during_discord_meeting
 */

public class ProductionCompany {
    private final int id;
    private final String logoPath;
    private final String name;
    private final String originCountry;

     public ProductionCompany(int id, String logoPath, String name, String originCountry) {
        this.id = id;
        this.logoPath = logoPath;
        this.name = name;
        this.originCountry = originCountry;
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

    public String getOriginCountry() {
        return originCountry;
    }
}