package actors.readability;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import services.features.readability.ReadabilityService;


/**
 * ReadabilityActor is an actor responsible for computing readability metrics
 * for a given text. It uses the {@link ReadabilityService} to calculate:
 * <ul>
 *     <li>Flesch Reading Ease score</li>
 *     <li>Flesch-Kincaid Grade Level</li>
 * </ul>
 * <p>
 * Clients send a {@link Compute} message containing the text to process
 * and an {@link ActorRef<Result>} to receive the computed scores.
 * </p>
 *
 * <p>This actor demonstrates the use of typed actors in Pekko (Akka) for
 * handling asynchronous computation in a thread-safe manner.</p>
 *
 * @author Ali Maher
 */
public class ReadabilityActor extends AbstractBehavior<ReadabilityActor.Command> {

    private final ReadabilityService readabilityService;

    // ===== Messages =====

    /**
     * Marker interface for all messages that {@link ReadabilityActor} can handle.
     */
    public interface Command {}

    /**
     * Command message to request readability computation.
     * Sent to {@link ReadabilityActor} with the text to analyze and a
     * reference to reply with the result.
     */
    public static class Compute implements Command {
        public final String overview;
        public final ActorRef<Result> replyTo;

        /**
         * Constructs a new Compute message.
         *
         * @param overview the text to analyze
         * @param replyTo the actor to send the result to
         */
        public Compute(String overview, ActorRef<Result> replyTo) {
            this.overview = overview;
            this.replyTo = replyTo;
        }
    }

    /**
     * Response message containing readability metrics.
     */
    public static class Result {
        public final double fleschScore;
        public final double gradeLevel;

        /**
         * Constructs a new Result message.
         *
         * @param fleschScore the Flesch Reading Ease score
         * @param gradeLevel the Flesch-Kincaid Grade Level
         */
        public Result(double fleschScore, double gradeLevel) {
            this.fleschScore = fleschScore;
            this.gradeLevel = gradeLevel;
        }
    }

    // ===== Actor =====

    /**
     * Factory method to create a {@link ReadabilityActor}.
     *
     * @param readabilityService the service used to compute readability scores
     * @return a {@link Behavior} for the actor
     */
    public static Behavior<Command> create(ReadabilityService readabilityService) {
        return Behaviors.setup(ctx -> new ReadabilityActor(ctx, readabilityService));
    }

    /**
     * Private constructor used by the factory method.
     *
     * @param context the actor context
     * @param readabilityService the service used to compute readability scores
     */
    private ReadabilityActor(
            ActorContext<Command> context,
            ReadabilityService readabilityService) {
        super(context);
        this.readabilityService = readabilityService;
    }

    /**
     * Defines how the actor handles incoming messages.
     *
     * @return the {@link Receive} for this actor
     */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Compute.class, this::onCompute)
                .build();
    }

    /**
     * Handles {@link Compute} messages by computing readability scores
     * and replying to the sender.
     *
     * @param msg the compute request containing text and reply actor
     * @return the same behavior instance (this) to continue receiving messages
     */
    private Behavior<Command> onCompute(Compute msg) {
        getContext().getLog().info("ReadabilityActor: start processing");
        double flesch = calculateFlesch(msg.overview);
        double grade = calculateGrade(msg.overview);

        msg.replyTo.tell(new Result(flesch, grade));
        return this;
    }

    /**
     * Calculates the Flesch Reading Ease score for a given text.
     *
     * @param text the text to analyze
     * @return the Flesch Reading Ease score
     */
    private double calculateFlesch(String text) {
        return readabilityService.calculateFleschReaddingEase(text);
    }

    /**
     * Calculates the Flesch Reading Ease score for a given text.
     *
     * @param text the text to analyze
     * @return the Flesch Reading Ease score
     */
    private double calculateGrade(String text) {
        return readabilityService.calculateFleschKincaidGradeLevel(text);
    }
}