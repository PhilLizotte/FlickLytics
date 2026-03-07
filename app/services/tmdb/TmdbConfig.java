package services.tmdb;

import com.typesafe.config.Config;
import jakarta.inject.Inject;

public class TmdbConfig {
    private final String baseUrl;
    private final String apiKey;
    private final String raToken;

    @Inject
    public TmdbConfig(Config config) {
        this.baseUrl = config.getString("tmdb.baseUrl");
        this.apiKey = config.hasPath("tmdb.apiKey") ? config.getString("tmdb.apiKey") : null;
        this.raToken = config.hasPath("tmdb.raToken") ? config.getString("tmdb.raToken") : null;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Missing TMDb API key. Set environment variable TMDB_API_KEY using " +
                    "\"$env:TMDB_API_KEY={your key here}\" or configure tmdb.apiKey.");
        }

        if (raToken == null || raToken.trim().isEmpty()) {
            throw new IllegalStateException("Missing TMDb read access token. Set environment variable TMDB_API_KEY " +
                    "using \"$env:TMDB_RA_TOKEN={your token here}\" or configure tmdb.raToken.");
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
    
    public String getRaToken() {
        return raToken;
    }
}
