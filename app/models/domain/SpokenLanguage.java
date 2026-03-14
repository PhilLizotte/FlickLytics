package models.domain;

/**
 * Domain model representing a spoken language.
 *
 * @author all_team_during_discord_meeting
 */
public class SpokenLanguage {

    /** Unique identifier of the language. */
    private final int id;

    /** Name of the language. */
    private final String name;

    /**
     * Creates a new {@code SpokenLanguage} instance.
     *
     * @param id the language identifier
     * @param name the language name
     */
    public SpokenLanguage(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the language identifier.
     *
     * @return the language ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the language name.
     *
     * @return the language name
     */
    public String getName() {
        return name;
    }
}