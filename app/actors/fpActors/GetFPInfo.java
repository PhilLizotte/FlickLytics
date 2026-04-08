package actors.fpActors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.actor.typed.ActorRef;

/**
 * @author Philippe Lizotte
 * Command message to request financial information calculation.
 * Sent to {@link FinancialPerformanceActor} with the needed info to
 * calculate all needed financial information.
 */
public class GetFPInfo implements FpCommand {
    public final ObjectNode info;
    public final ActorRef<FpResult> replyTo;

    /**
     * Simple constructor.
     * 
     * @param info the json returned from the service's API call
     * @param replyTo where the requested information will have to be sent
     */
    public GetFPInfo(ObjectNode info, ActorRef<FpResult> replyTo) {
        this.info = info;
        this.replyTo = replyTo;
    }
}
