package models.domain;

/**
 * Domain model representing a creator associated with a movie or TV show.
 * <p>
 * This class stores information about individuals who participated in the
 * creation of a media item (for example writers, directors, or producers).
 * The attributes are derived from the fields defined in the project's
 * appendix specification.
 *
 * @author all_team_during_discord_meeting
 */
public class Creator {

    /**
     * Unique identifier of the creator.
     */
    private final int id;

    /**
     * Unique credit identifier associated with the creator's role.
     */
    private final String credit_id;

    /**
     * Name of the creator.
     */
    private final String name;

    /**
     * Gender identifier of the creator.
     * Typically follows the API gender convention (e.g., 0 = unknown, 1 = female, 2 = male).
     */
    private int gender;

    /**
     * Path to the creator's profile image.
     */
    private final String profile_path;

    /**
     * Constructs a new {@code Creator} instance with the specified attributes.
     *
     * @param id the unique identifier of the creator
     * @param credit_id the credit identifier associated with the creator
     * @param name the name of the creator
     * @param gender the gender identifier of the creator
     * @param profile_path the path to the creator's profile image
     */
    public Creator(int id, String credit_id, String name, int gender, String profile_path) {
        this.id = id;
        this.credit_id = credit_id;
        this.name = name;
        this.gender = gender;
        this.profile_path = profile_path;
    }

    /**
     * Returns the unique identifier of the creator.
     *
     * @return the creator's ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the credit identifier associated with the creator.
     *
     * @return the credit ID
     */
    public String getCredit_id() {
        return credit_id;
    }

    /**
     * Returns the name of the creator.
     *
     * @return the creator's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the gender identifier of the creator.
     *
     * @return the gender value
     */
    public int getGender() {
        return gender;
    }

    /**
     * Returns the path to the creator's profile image.
     *
     * @return the profile image path
     */
    public String getProfile_path() {
        return profile_path;
    }
}