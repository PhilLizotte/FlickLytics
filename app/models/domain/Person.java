package models.domain;

/**
 * Domain model representing a person from TMDb.
 * <p>
 * This class encapsulates basic information about an individual such as
 * an actor, director, or crew member obtained from the TMDb dataset.
 * It includes identification information, department association,
 * profile image reference, and popularity metrics.
 *
 * @author all_team_during_discord_meeting
 */
public class Person {

    /** Unique identifier of the person. */
    private final int id;

    /** Name of the person. */
    private final String name;

    /** Path to the person's profile image. */
    private final String profilePath;

    /** Gender description of the person. */
    private final String gender;

    /** Department the person is primarily known for (e.g., Acting, Directing). */
    private final String knownForDepartment;

    /** Popularity score of the person. */
    private final double popularity;

    /**
     * Constructs a {@code Person} domain object.
     *
     * @param id the unique identifier of the person
     * @param name the name of the person
     * @param profilePath the path to the person's profile image
     * @param gender the gender description of the person
     * @param knownForDepartment the department the person is known for
     * @param popularity the popularity score of the person
     */
    public Person(
            int id,
            String name,
            String profilePath,
            String gender,
            String knownForDepartment,
            double popularity
    ) {
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
        this.gender = gender;
        this.knownForDepartment = knownForDepartment;
        this.popularity = popularity;
    }

    /**
     * Returns the unique identifier of the person.
     *
     * @return the person ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the person.
     *
     * @return the person's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the path to the person's profile image.
     *
     * @return the profile image path
     */
    public String getProfilePath() {
        return profilePath;
    }

    /**
     * Returns the gender description of the person.
     *
     * @return the person's gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Returns the department the person is primarily known for.
     *
     * @return the department associated with the person
     */
    public String getKnownForDepartment() {
        return knownForDepartment;
    }

    /**
     * Returns the popularity score of the person.
     *
     * @return the popularity score
     */
    public double getPopularity() {
        return popularity;
    }
}