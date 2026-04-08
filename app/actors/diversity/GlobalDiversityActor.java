// File: app/actors/diversity/GlobalDiversityActor.java
package actors.diversity;

import jakarta.inject.Inject;
import models.dto.GlobalDiversityStats;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import services.features.diversity.GlobalDiversityService;

import java.util.concurrent.CompletionStage;

/**
 * Pekko typed actor responsible for computing Global Diversity metrics.
 *
 * <p>This actor is a thin async wrapper around {@link GlobalDiversityService}:
 * it receives a request, triggers the async computation, then replies back to
 * the provided {@link ActorRef} with either {@link Ok} or {@link Error}.</p>
 *
 * <p>Design goals:
 * <ul>
 *   <li>Non-blocking: never calls join()/get().</li>
 *   <li>Testable: service can be mocked and injected.</li>
 *   <li>Clear protocol: Command/Response message classes.</li>
 * </ul>
 * </p>
 *
 * @author Chama
 */
public class GlobalDiversityActor extends AbstractBehavior<GlobalDiversityActor.Command> {

    /** Marker interface for incoming actor messages. */
    public interface Command {}

    /**
     * Request message: compute global diversity for a TMDb entity.
     * Caller supplies {@code replyTo} so we can respond asynchronously.
     */
    public static final class Compute implements Command {
        public final String category; // "movie" or "tv"
        public final int id;
        public final ActorRef<Response> replyTo;

        public Compute(String category, int id, ActorRef<Response> replyTo) {
            this.category = category;
            this.id = id;
            this.replyTo = replyTo;
        }
    }

    /** Marker interface for outgoing replies. */
    public interface Response {}

    /** Success reply with computed stats. */
    public static final class Ok implements Response {
        public final GlobalDiversityStats stats;

        public Ok(GlobalDiversityStats stats) {
            this.stats = stats;
        }
    }

    /** Error reply with a user-facing error message. */
    public static final class Error implements Response {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }

    /**
     * Internal message used to bring async results back into the actor mailbox.
     * Standard Pekko pattern: pipe async result to self, then handle it.
     */
    private static final class WrappedResult implements Command {
        final ActorRef<Response> replyTo;
        final GlobalDiversityStats stats; // may be null
        final Throwable error;            // may be null

        WrappedResult(ActorRef<Response> replyTo, GlobalDiversityStats stats, Throwable error) {
            this.replyTo = replyTo;
            this.stats = stats;
            this.error = error;
        }
    }

    private final GlobalDiversityService globalDiversityService;

    /** Actor factory method. */
    public static Behavior<Command> create(GlobalDiversityService service) {
        return Behaviors.setup(ctx -> new GlobalDiversityActor(ctx, service));
    }

    @Inject
    public GlobalDiversityActor(ActorContext<Command> context, GlobalDiversityService globalDiversityService) {
        super(context);
        this.globalDiversityService = globalDiversityService;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Compute.class, this::onCompute)
                .onMessage(WrappedResult.class, this::onWrappedResult)
                .build();
    }

    private Behavior<Command> onCompute(Compute msg) {
        // Validate inputs early (fast fail)
        if (msg.category == null || msg.category.trim().isEmpty()) {
            msg.replyTo.tell(new Error("Missing category"));
            return this;
        }
        if (!"movie".equalsIgnoreCase(msg.category) && !"tv".equalsIgnoreCase(msg.category)) {
            msg.replyTo.tell(new Error("Invalid category. Expected 'movie' or 'tv'."));
            return this;
        }
        if (msg.id <= 0) {
            msg.replyTo.tell(new Error("Invalid id"));
            return this;
        }

        CompletionStage<GlobalDiversityStats> stage =
                globalDiversityService.compute(msg.category, msg.id);

        getContext().pipeToSelf(
                stage,
                (stats, ex) -> new WrappedResult(msg.replyTo, stats, ex)
        );

        return this;
    }

    private Behavior<Command> onWrappedResult(WrappedResult msg) {
        if (msg.error != null) {
            getContext().getLog().error("Global diversity computation failed", msg.error);
            msg.replyTo.tell(new Error("Global diversity computation failed"));
            return this;
        }

        if (msg.stats == null) {
            msg.replyTo.tell(new Error("Global diversity computation failed"));
            return this;
        }

        msg.replyTo.tell(new Ok(msg.stats));
        return this;
    }
}