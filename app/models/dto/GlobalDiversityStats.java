package models.dto;

/**
 * DTO carrying computed "Global Diversity" metrics for a TMDb entity.
 *
 * <p>Used by the controller to render the globalDiversity view.
 *
 * @author Chama
 */
public class GlobalDiversityStats {
    public final String category;
    public final int id;

    public final int translationCount;
    public final int supportedLanguageCount;

    public final double translationDensity;
    public final double localizationIndex;

    /**
     * Creates a GlobalDiversityStats DTO.
     *
     * @param category "movie" or "tv"
     * @param id TMDb entity id
     * @param translationCount number of translation entries returned by TMDb translations endpoint
     * @param supportedLanguageCount number of supported languages returned by TMDb configuration/languages
     * @param translationDensity translationCount / supportedLanguageCount (0 if supportedLanguageCount == 0)
     * @param localizationIndex average ratio of translated overview length to baseline overview length
     */
    public GlobalDiversityStats(
            String category,
            int id,
            int translationCount,
            int supportedLanguageCount,
            double translationDensity,
            double localizationIndex
    ) {
        this.category = category;
        this.id = id;
        this.translationCount = translationCount;
        this.supportedLanguageCount = supportedLanguageCount;
        this.translationDensity = translationDensity;
        this.localizationIndex = localizationIndex;
    }
}