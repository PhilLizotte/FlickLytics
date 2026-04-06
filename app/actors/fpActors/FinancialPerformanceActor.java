package actors.fpActors;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.*;
import org.apache.pekko.pattern.StatusReply;

import java.time.Duration;

public class FinancialPerformanceActor extends AbstractBehavior<FinancialPerformanceActor.GetFPInfo> {

    public static class GetFPInfo {
        public final int id;

        public GetFPInfo(int id) {
            this.id = id;
        }
    }

    public static Behavior<FinancialPerformanceActor.GetFPInfo> create() {
        return Behaviors.setup(FinancialPerformanceActor::new);
    }

    private FinancialPerformanceActor(ActorContext<FinancialPerformanceActor.GetFPInfo> context) {
        super(context);
    }

    @Override
    public Receive<FinancialPerformanceActor.GetFPInfo> createReceive() {
        return newReceiveBuilder()
                .onMessage(FinancialPerformanceActor.GetFPInfo.class, this::onGetFPInfo).build();
    }

    private Behavior<FinancialPerformanceActor.GetFPInfo> onGetFPInfo(FinancialPerformanceActor.GetFPInfo command) {
        getContext().getLog().info("Undelayed: This actor now has id {}", command.id);
        
        // replace getSender() with a replyTo field
        // - But that's an actor... how do I make this do something from the field?
        
        // I might have to think things over from scratch.
        // Probably need to call the page render function from within this method.
        // ... and somehow build a response object in the controller method?
        
        
        // Response is a JSON object with all the required fields
        // getSender().tell(response, this);
        
        return this;
    }
}
