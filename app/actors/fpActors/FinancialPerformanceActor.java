package actors.fpActors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.*;
import services.features.financial.FinancialPerformanceService;

public class FinancialPerformanceActor extends AbstractBehavior<FpCommand> {

    /**
     * Standard factory architecture FpCommand behavior
     * 
     * @return FpCommand behavior
     */
    public static Behavior<FpCommand> create() {
        return Behaviors.setup(FinancialPerformanceActor::new);
    }

    /**
     * Standard constructor
     * 
     * @param context the actor context
     */
    private FinancialPerformanceActor(
            ActorContext<FpCommand> context) {
        super(context);
    }

    /**
     * Standard actor action reactor, specifically when receiving an FpCommand
     * 
     * @return the return parameter of onGetFPInfo()
     */
    @Override
    public Receive<FpCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(FpCommand.class, this::onGetFPInfo).build();
    }

    /**
     * Calculates a movie's net profit, ROI percent and financial rating based off
     * of its budget and revenue.
     * 
     * @param command behavior object containing movie json.
     * @return Result behavior, containing all financial information of the movie.
     */
    private Behavior<FpCommand> onGetFPInfo(FpCommand command) {
        ObjectNode info = ((GetFPInfo) command).info;
        getContext().getLog().info("Getting financial info for movie with id {}...", info.get("id").asInt());

        int budget = info.get("budget").asInt();
        int revenue = info.get("revenue").asInt();

        int netProfit = revenue - budget;
        String roiPercent;
        float roiPercentNum;
        String financialRating;
        if (budget == 0) {
            roiPercent = "Unknown; This movie has no recorded budget.";
            financialRating = "Unknown";
        } else {
            roiPercentNum = (float) (100 * ((double)netProfit / (double)budget));
            roiPercent = String.format("%.2f", roiPercentNum);
            if (roiPercentNum < 0) {
                financialRating = "Financial Loss";
            } else if (roiPercentNum < 200) {
                financialRating = "Profitable";
            } else if (roiPercentNum < 500) {
                financialRating = "High Return";
            } else {
                financialRating = "Blockbuster Success";
            }
        }

        ((GetFPInfo) command).replyTo.tell(new FpResult(
                info.get("title").asText(),
                String.valueOf(netProfit),
                roiPercent,
                financialRating
        ));
        
        return this;
    }
}
