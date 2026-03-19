package models.domain;

/**
 * Domain model representing a television network.
 * <p>
 * This class stores information about a network responsible for
 * broadcasting or producing TV shows, including its identifier,
 * logo, name, and country of origin.
 *
 * @author all_team_during_discord_meeting
 */
public class Network {

    /** Unique identifier of the network. */
    private final int id;

    /** Path to the network's logo image. */
    private final String logoPath;

    /** Name of the network. */
    private final String name;

    /** Country where the network originates from. */
    private final String origin_country;

    /**
     * Creates a new {@code Network} instance.
     *
     * @param id the unique identifier of the network
     * @param logoPath the path to the network's logo image
     * @param name the name of the network
     * @param origin_country the country of origin of the network
     */
    public Network(int id, String logoPath, String name, String origin_country) {
        this.id = id;
        this.logoPath = logoPath;
        this.name = name;
        this.origin_country = origin_country;
    }

    /**
     * Returns the unique identifier of the network.
     *
     * @return the network ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the path to the network's logo image.
     *
     * @return the logo path
     */
    public String getLogoPath() {
        return logoPath;
    }

    /**
     * Returns the name of the network.
     *
     * @return the network name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the country where the network originates from.
     *
     * @return the origin country of the network
     */
    public String getOrigin_country() {
        return origin_country;
    }
}