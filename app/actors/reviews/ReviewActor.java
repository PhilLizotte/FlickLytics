package actors.reviews;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import models.domain.Review;
import services.features.reviews.ReviewSentimentService;

import java.util.List;

/**
 * ReviewActor is the actor responsible for computing the review sentiment.
 * For a given list of (up to 50) reviews, it uses the
 * {@link ReviewSentimentService}
 * to calculate the overall and per-review sentiments.
 * 
 * @author Craig Kogan
 */
public class ReviewActor extends AbstractBehavior<ReviewActor.Command> {

    private final ReviewSentimentService reviewService;

    // ===== Messages =====

    /**
     * Marker interface for all messages that {@link ReviewActor} can handle.
     */
    public interface Command {
    }

    /**
     * Command message to request review sentiment processing.
     */
    public static class Compute implements Command {
        public final List<String> reviewsList;
        public final ActorRef<Result> replyTo;

        /**
         * Constructs a new Compute message.
         *
         * @param reviewsList the list of reviews to analyze
         * @param replyTo     the actor to send the result to
         */
        public Compute(List<String> reviewsList, ActorRef<Result> replyTo) {
            this.reviewsList = reviewsList;
            this.replyTo = replyTo;
        }
    }

    /**
     * Response message containing review sentiments.
     */
    public static class Result {
        public final Review review;

        /**
         * Constructs a new Result message.
         *
         * @param review a Review object which contains all information about the
         *               reviews, including the sentiments.
         */
        public Result(Review review) {
            this.review = review;
        }
    }

    // ===== Actor =====

    /**
     * Factory method to create a {@link ReviewActor}.
     *
     * @param reviewSentimentService the service used to determine review
     *                               sentiments.
     * @return a {@link Behavior} for the actor
     */
    public static Behavior<Command> create(ReviewSentimentService reviewSentimentService) {
        return Behaviors.setup(ctx -> new ReviewActor(ctx, reviewSentimentService));
    }

    /**
     * Private constructor used by the factory method.
     * 
     * @param ctx                    the actor context
     * @param reviewSentimentService the reviewSentimentService used to determine
     *                               review sentiments
     */
    private ReviewActor(
            ActorContext<Command> ctx,
            ReviewSentimentService reviewSentimentService) {
        super(ctx);
        this.reviewService = reviewSentimentService;
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

    private Behavior<Command> onCompute(Compute msg) {
        getContext().getLog().info("ReviewActor: start processing");
        Review review = processReviews(msg.reviewsList);

        // NOTE:: Look into this future me!
        // So, I was really just following along what Ali did in the Readability actor.
        // Since I have everything bundled into a single variable already,
        // do I still need the Result type?

        msg.replyTo.tell(new Result(review));
        return this;
    }

    /**
     * Processes reviews to determine sentiments.
     * 
     * @param reviews The list of raw reviews to be processed.
     * @return A {@Link Review} object that contains all information about reviews,
     *         both raw and processed.
     */
    private Review processReviews(List<String> reviews) {
        return reviewService.extractSentiment(reviews);
    }

}
