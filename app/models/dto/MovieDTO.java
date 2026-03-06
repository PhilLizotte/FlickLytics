package models.dto;

import models.domain.Genre;
import models.domain.ProductionCompany;
import models.domain.SpokenLanguage;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * This is DTO representing a Movie returned by TMDb API.
 *
 * @author all_team_during_discord_meeting
 */

public class MovieDTO {
    public boolean adult;
    public String backdrop_path;
    public boolean belongs_to_collection;
    public long budget;
    public List<Genre> genres;
    public String homepage;
    public int id;
    public int imdb_id;
    public String original_language;
    public String original_title;
    public String overview;
    public double popularity;
    public String poster_path;
    public List<ProductionCompany> production_companies;
    public LocalDate release_date;
    public long revenue;
    public int runtime;
    public List<SpokenLanguage> spoken_languages;
    public String status;
    public String tagline;
    public String title;
    public boolean video;
    public double vote_average;
    public int vote_count;
}