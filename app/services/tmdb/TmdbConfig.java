package services.tmdb;

import com.typesafe.config.Config;
import jakarta.inject.Inject;

public class TmdbConfig {
    private final String baseUrl;
    private final String apiKey;

    @Inject
    public TmdbConfig(Config config) {
        this.baseUrl = config.getString("tmdb.baseUrl");
        this.apiKey = config.hasPath("tmdb.apiKey") ? config.getString("tmdb.apiKey") : null;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Missing TMDb API key. Set environment variable TMDB_API_KEY or configure tmdb.apiKey.");
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
