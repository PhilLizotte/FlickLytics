package services.tmdb;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link TmdbConfig}.
 * <p>
 * Verifies that configuration values are read correctly and that missing
 * required configuration values throw informative exceptions.
 * </p>
 *
 * @author Aram Zand
 */
public class TmdbConfigTest {

    @Test
    public void testTmdbConfigHappyPath() {
        Config config = ConfigFactory.parseString(
                "tmdb.baseUrl=\"https://api.themoviedb.org/3\"\n" +
                "tmdb.apiKey=\"test_api_key\"\n" +
                "tmdb.raToken=\"test_ra_token\"\n"
        );

        TmdbConfig tmdbConfig = new TmdbConfig(config);

        assertEquals("https://api.themoviedb.org/3", tmdbConfig.getBaseUrl());
        assertEquals("test_api_key", tmdbConfig.getApiKey());
        assertEquals("test_ra_token", tmdbConfig.getRaToken());
    }

    @Test
    public void testTmdbConfigMissingApiKeyThrows() {
        Config config = ConfigFactory.parseString(
                "tmdb.baseUrl=\"https://api.themoviedb.org/3\"\n" +
                "tmdb.raToken=\"test_ra_token\"\n"
        );

        try {
            new TmdbConfig(config);
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Missing TMDb API key"));
            return;
        }

        throw new AssertionError("Expected IllegalStateException for missing tmdb.apiKey");
    }

    @Test
    public void testTmdbConfigMissingRaTokenThrows() {
        Config config = ConfigFactory.parseString(
                "tmdb.baseUrl=\"https://api.themoviedb.org/3\"\n" +
                "tmdb.apiKey=\"test_api_key\"\n"
        );

        try {
            new TmdbConfig(config);
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("Missing TMDb read access token"));
            return;
        }

        throw new AssertionError("Expected IllegalStateException for missing tmdb.raToken");
    }
}
