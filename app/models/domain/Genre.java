package models.domain;

/**
 * Domain model representing a genre associated with a movie or TV show.
 *
 * @author all_team_during_discord_meeting
 */
public class Genre {

    /** Unique identifier of the genre. */
    private final int id;

    /** Name of the genre. */
    private final String name;

    /**
     * Creates a new {@code Genre} instance.
     *
     * @param id the unique identifier of the genre
     * @param name the name of the genre
     */
    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the unique identifier of the genre.
     *
     * @return the genre ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the genre.
     *
     * @return the genre name
     */
    public String getName() {
        return name;
    }
}