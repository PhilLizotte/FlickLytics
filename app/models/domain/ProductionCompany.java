package models.domain;

/**
 * Domain model representing a production company involved in a movie or TV show.
 *
 * @author all_team_during_discord_meeting
 */
public class ProductionCompany {

    /** Unique identifier of the production company. */
    private final int id;

    /** Path to the company's logo image. */
    private final String logoPath;

    /** Name of the production company. */
    private final String name;

    /** Country where the production company originates from. */
    private final String originCountry;

    /**
     * Creates a new {@code ProductionCompany} instance.
     *
     * @param id the unique identifier of the production company
     * @param logoPath the path to the company's logo image
     * @param name the name of the production company
     * @param originCountry the country of origin of the production company
     */
    public ProductionCompany(int id, String logoPath, String name, String originCountry) {
        this.id = id;
        this.logoPath = logoPath;
        this.name = name;
        this.originCountry = originCountry;
    }

    /**
     * Returns the unique identifier of the production company.
     *
     * @return the company ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the path to the company's logo image.
     *
     * @return the logo path
     */
    public String getLogoPath() {
        return logoPath;
    }

    /**
     * Returns the name of the production company.
     *
     * @return the company name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the country of origin of the production company.
     *
     * @return the origin country
     */
    public String getOriginCountry() {
        return originCountry;
    }
}