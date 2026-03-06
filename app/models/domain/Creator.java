package models.domain;

/**
 * This is domain model for representing Movie.
 * The fields are based on the Appendix of the Project Document.
 *
 * @author  all_team_during_discord_meeting
 */

public class Creator {
    private final int id;
    private final String credit_id;
    private final String name;
    private int gender;
    private final String profile_path;

    public Creator(int id, String credit_id, String name, int gender, String profile_path) {
        this.id = id;
        this.credit_id = credit_id;
        this.name = name;
        this.gender = gender;
        this.profile_path = profile_path;
        );
    }


    public int getId() {
        return id;
    }

    public String getCredit_id() {
        return credit_id;
    }

    public String getName() {
        return name;
    }

    public int getGender() {
        return gender;
    }

    public String getProfile_path() {
        return profile_path;
    }
}
