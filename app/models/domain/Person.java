package models.domain;


/**
 * This is domain model representing a Person from TMDb.
 *
 * @author all_team_during_discord_meeting
 */

public class Person {
    private final int id;
    private final String name;
    private final String profilePath;
    private final String gender;
    private final String knownForDepartment;
    private final double popularity;

    /**
     * Constructs a Person domain object.
     *
     * @param id person ID
     * @param name person name
     * @param profilePath profile image path
     * @param gender gender description
     * @param knownForDepartment department known for
     * @param popularity popularity score
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
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getProfilePath() {
        return profilePath;
    }
    
    public String getGender() {
        return gender;
    }
    
    public String getKnownForDepartment() {
        return knownForDepartment;
    }
    
    public double getPopularity() {
        return popularity;
    }
}