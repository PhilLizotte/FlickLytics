package models.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class MovieTest {

    @Test
    public void testMovieDomain() {
        Movie movie = new Movie(
                1,
                "Batman",
                "A dark knight story",
                LocalDate.of(2008, 7, 18),
                List.of(new Genre(1, "Action")),
                "https://example.com",
                9.5,
                "/poster.jpg",
                List.of(new ProductionCompany(1, "Warner Bros", "Warner Bros", "Warner Bros")),
                1000000000L,
                120,
                List.of(new SpokenLanguage(1, "English")),
                "Released",
                "Why so serious?",
                8.9,
                20000
        );

        assertEquals(1, movie.getId());
        assertEquals("Batman", movie.getName());
        assertEquals("A dark knight story", movie.getOverview());
        assertEquals(LocalDate.of(2008, 7, 18), movie.getReleaseDate());
        assertEquals(1, movie.getGenres().size());
        assertEquals("https://example.com", movie.getHomepage());
        assertEquals(9.5, movie.getPopularity(), 0.01);
        assertEquals("/poster.jpg", movie.getPosterPath());
        assertEquals(1, movie.getProductionCompanies().size());
        assertEquals(1000000000L, movie.getRevenue());
        assertEquals(120, movie.getRuntime());
        assertEquals(1, movie.getSpokenLanguages().size());
        assertEquals("Released", movie.getStatus());
        assertEquals("Why so serious?", movie.getTagline());
        assertEquals(8.9, movie.getVoteAverage(), 0.01);
        assertEquals(20000, movie.getVoteCount());
    }

    @Test
    public void testGenreDomain() {
        Genre genre = new Genre(1, "Action");
        assertEquals(1, genre.getId());
        assertEquals("Action", genre.getName());
    }

    @Test
    public void testProductionCompanyDomain() {
        ProductionCompany company = new ProductionCompany(1, "Warner Bros", "Warner Bros", "Warner Bros");
        assertEquals(1, company.getId());
        assertEquals("Warner Bros", company.getLogoPath());
        assertEquals("Warner Bros", company.getName());
        assertEquals("Warner Bros", company.getOriginCountry());
    }

    @Test
    public void testSpokenLanguageDomain() {
        SpokenLanguage language = new SpokenLanguage(1, "English");
        assertEquals(1, language.getId());
        assertEquals("English", language.getName());
    }

    @Test
    public void testNetwork() {
        Network network = new Network(1, "https://hbo.com/logo.png", "HBO", "USA");
        assertEquals(1, network.getId());
        assertEquals("https://hbo.com/logo.png", network.getLogoPath());
        assertEquals("HBO", network.getName());
        assertEquals("USA", network.getOrigin_country());
    }
    
    @Test
    public void testCreator() {
        Creator creator = new Creator(1, "12hukj12l", "Christopher Nolan", 0, "https://example.com/nolan.jpg");
        assertEquals(1, creator.getId());
        assertEquals("12hukj12l", creator.getCredit_id());
        assertEquals("Christopher Nolan", creator.getName());
        assertEquals(0, creator.getGender());
        assertEquals("https://example.com/nolan.jpg", creator.getProfile_path());
    }
}
