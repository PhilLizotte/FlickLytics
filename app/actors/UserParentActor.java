package actors;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pekko.NotUsed;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.stream.javadsl.Flow;

/**
 * Parent actor responsible for managing and creating {@link UserActor} instances.
 * <p>
 * This actor acts as a factory for spawning child {@code UserActor}s based on a unique user ID.
 * When a {@link Create} command is received, it creates a new {@code UserActor} and initializes it
 * by sending a {@link UserActor.Init} message.
 * </p>
 *
 * <p>
 * Typical usage:
 * <ul>
 *     <li>Receives a WebSocket connection request</li>
 *     <li>Spawns a dedicated {@code UserActor} for that connection</li>
 *     <li>Connects the actor to a reactive {@link Flow}</li>
 * </ul>
 * </p>
 *
 * @author Ali Maher
 */
public class UserParentActor {

    /**
     * Marker interface for all commands that {@link UserParentActor} can handle.
     */
    public interface Command {}

    /**
     * Command used to create a new {@link UserActor}.
     */
    public static final class Create implements Command {
        public final String id;
        public final ActorRef<Flow<JsonNode, JsonNode, NotUsed>> replyTo;

        /**
         * Constructs a Create command.
         *
         * @param id unique user identifier
         * @param replyTo actor reference that will receive the created Flow
         */
        public Create(String id, ActorRef<Flow<JsonNode, JsonNode, NotUsed>> replyTo) {
            this.id = id;
            this.replyTo = replyTo;
        }
    }

    /**
     * Creates the behavior of the {@link UserParentActor}.
     *
     * <p>
     * This behavior listens for {@link Create} messages and:
     * <ol>
     *     <li>Spawns a new {@link UserActor} with a unique name</li>
     *     <li>Sends an {@link UserActor.Init} message to initialize it</li>
     * </ol>
     * </p>
     *
     * @return the behavior of this actor
     */
    public static Behavior<Command> create() {
        return Behaviors.setup(context ->
                Behaviors.receive(Command.class)
                        .onMessage(Create.class, msg -> {
                            ActorRef<UserActor.Message> userActor =
                                    context.spawn(UserActor.create(msg.id), "user-" + msg.id);

                            userActor.tell(new UserActor.Init(msg.replyTo));

                            return Behaviors.same();
                        })
                        .build()
        );
    }
}