package actors.fpActors;

/**
 * @author Philippe Lizotte
 * Simple wrapper object containing all information needed of the financial information feature.
 * Used by {@link FinancialPerformanceActor}
 */
public class FpResult {
    public final String title;
    public final String netProfit;
    public final String roi;
    public final String financialStatus;

    /**
     * Simple Constructor
     * 
     * @param title Movie title
     * @param netProfit Net profit (difference between revenue and budget)
     * @param roi What percent of the budget was recieved in profit.
     * @param financialStatus A description of the movie's status, based on ROI.
     */
    public FpResult(String title, String netProfit, String roi, String financialStatus) {
        this.title = title;
        this.netProfit = netProfit;
        this.roi = roi;
        this.financialStatus = financialStatus;
    }
}
