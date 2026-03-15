package models.dto;

public class GlobalDiversityStats {
    public final String category;
    public final int id;

    public final int translationCount;
    public final int supportedLanguageCount;

    public final double translationDensity;
    public final double localizationIndex;

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