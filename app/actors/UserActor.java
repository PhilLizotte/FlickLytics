package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.NotUsed;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.japi.Pair;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.stream.javadsl.*;
import play.libs.Json;

import java.time.Duration;
import java.util.*;

/**
 * Actor responsible for handling a single user's WebSocket session.
 *
 * <p>
 * This actor manages:
 * <ul>
 *     <li>Incoming WebSocket JSON messages</li>
 *     <li>Processing search requests</li>
 *     <li>Streaming results back to the client using reactive streams</li>
 *     <li>Deduplication of streamed results</li>
 * </ul>
 * </p>
 *
 * <p>
 * Architecture:
 * <pre>
 * WebSocket → Sink → Actor (IncomingMessage)
 * Actor → Hub (MergeHub + BroadcastHub) → WebSocket Source
 * </pre>
 * </p>
 *
 * <p>
 * Each user gets:
 * <ul>
 *     <li>A dedicated actor instance</li>
 *     <li>A dynamic stream pipeline</li>
 *     <li>Periodic polling for new data</li>
 * </ul>
 * </p>
 *
 * @author Ali Maher
 *
 */
public class UserActor {

    // =======================
    // Messages
    // =======================

    /**
     * Marker interface for all messages handled by {@link UserActor}.
     */
    public interface Message {}

    /**
     * Initialization message sent by {@link actors.UserParentActor}.
     * <p>
     * Provides a reference to send back the created WebSocket {@link Flow}.
     * </p>
     */
    public static final class Init implements Message {
        public final ActorRef<Flow<JsonNode, JsonNode, NotUsed>> replyTo;

        /**
         * @param replyTo actor that will receive the WebSocket flow
         */
        public Init(ActorRef<Flow<JsonNode, JsonNode, NotUsed>> replyTo) {
            this.replyTo = replyTo;
        }
    }


    /**
     * Internal message representing incoming JSON from the WebSocket.
     */
    private static final class IncomingMessage implements Message {
        public final JsonNode json;

        public IncomingMessage(JsonNode json) {
            this.json = json;
        }
    }

    /**
     * Internal periodic message used to trigger streaming updates.
     */
    private static final class StreamTick implements Message {}

    /**
     * Internal message used to stop the actor when the WebSocket is closed.
     */
    private static final class InternalStop implements Message {}

    // =======================
    // State
    // =======================

    private final String userId;
    private final ActorContext<Message> context;
    private final Materializer mat;

    private final Sink<JsonNode, NotUsed> hubSink;
    private final Source<JsonNode, NotUsed> hubSource;

    private final Set<String> seenIds = new HashSet<>();
    private String currentQuery = "";

    // =======================
    // Constructor
    // =======================

    /**
     * Creates a new {@link UserActor}.
     *
     * <p>
     * Initializes a dynamic streaming hub using:
     * <ul>
     *     <li>{@link MergeHub} for multiple producers</li>
     *     <li>{@link BroadcastHub} for multiple consumers</li>
     * </ul>
     * </p>
     *
     * @param userId unique user identifier
     * @param context actor context
     */
    private UserActor(String userId, ActorContext<Message> context) {
        this.userId = userId;
        this.context = context;
        this.mat = Materializer.matFromSystem(context.getSystem());

        // Hub
        Pair<Sink<JsonNode, NotUsed>, Source<JsonNode, NotUsed>> hub =
                MergeHub.of(JsonNode.class, 16)
                        .toMat(BroadcastHub.of(JsonNode.class, 256), Keep.both())
                        .run(mat);

        this.hubSink = hub.first();
        this.hubSource = hub.second();
        // for debugging >>>
//        this.hubSource = hub.second().map(json -> {
//            System.out.println("Stream passed: " + json);
//            return json;
//        });
    }

    // =======================
    // Factory
    // =======================

    /**
     * Factory method to create the actor behavior.
     *
     * @param userId unique user identifier
     * @return actor behavior
     */
    public static Behavior<Message> create(String userId) {
        return Behaviors.setup(context -> new UserActor(userId, context).behavior());
    }

    // =======================
    // Behavior
    // =======================

    /**
     * Defines the main behavior of the actor.
     *
     * @return behavior handling all incoming messages
     */
    private Behavior<Message> behavior() {
        return Behaviors.receive(Message.class)
                // WebSocket init
                .onMessage(Init.class, msg -> {
                    Flow<JsonNode, JsonNode, NotUsed> flow =
                            createWebSocketFlow();
                    msg.replyTo.tell(flow);
                    return Behaviors.same();
                })

                // incoming JSON
                .onMessage(IncomingMessage.class, msg -> {
                    JsonNode json = msg.json;
                    // for debugging >>>
                    // context.getLog().info("Received JSON: {}", json.toString());

                    if (!json.has("type")) {
                        sendError("Missing type");
                        return Behaviors.same();
                    }

                    String type = json.get("type").asText();

                    switch (type) {
                        case "search":
                            handleSearch(json);
                            break;
                        default:
                            sendError("Unknown type: " + type);
                    }
                    return Behaviors.same();
                })

                // Periodic streaming trigger
                .onMessage(StreamTick.class, msg -> {
                    fetchAndPushData();
                    return Behaviors.same();
                })

                // Stop actor
                .onMessageEquals(new InternalStop(), () -> Behaviors.stopped())
                .build();
    }

    // =======================
    // WebSocket Flow
    // =======================

    /**
     * Creates the WebSocket flow for this user.
     *
     * <p>
     * The flow:
     * <ul>
     *     <li>Consumes incoming JSON and forwards it to the actor</li>
     *     <li>Streams outgoing JSON from the hub to the client</li>
     *     <li>Stops the actor when the stream terminates</li>
     * </ul>
     * </p>
     *
     * @return WebSocket flow
     */
    private Flow<JsonNode, JsonNode, NotUsed> createWebSocketFlow() {
        Sink<JsonNode, NotUsed> jsonSink =
                Flow.<JsonNode>create()
                        .map(msg -> {
                            context.getSelf().tell(new IncomingMessage(msg));
                            return NotUsed.getInstance();
                        })
                        .to(Sink.ignore());

        Flow<JsonNode, JsonNode, NotUsed> flow =
                Flow.fromSinkAndSourceCoupled(jsonSink, hubSource)
                        .watchTermination((notUsed, stage) -> {
                            context.pipeToSelf(stage, (done, ex) -> new InternalStop());
                            return NotUsed.getInstance();
                        });

        return flow;
    }

    // =======================
    // Search Handling
    // =======================

    /**
     * Handles a search request from the client.
     *
     * <p>
     * This method:
     * <ul>
     *     <li>Extracts the query</li>
     *     <li>Resets deduplication state</li>
     *     <li>Fetches initial results</li>
     *     <li>Starts periodic polling</li>
     * </ul>
     * </p>
     *
     * @param json incoming JSON request
     */
    private void handleSearch(JsonNode json) {
        currentQuery = json.findPath("query").asText("");
        seenIds.clear();
        context.getLog().info("New search: {}", currentQuery);
        // initial load
        fetchAndPushData();

        // start streaming (poll every 5 sec)
        Source.tick(Duration.ofSeconds(5), Duration.ofSeconds(5), new StreamTick())
                .runForeach(msg -> context.getSelf().tell(msg), mat);
    }

    // =======================
    // Fake TMDb (replace later)
    // =======================

    /**
     * Fetches data and pushes new (non-duplicate) results to the stream.
     */
    private void fetchAndPushData() {
        if (currentQuery.isEmpty()) return;
        List<JsonNode> results = fakeApiCall(currentQuery);
        results.stream()
                .filter(item -> !seenIds.contains(item.get("id").asText()))
                .forEach(item -> {
                    seenIds.add(item.get("id").asText());
                    hubSink.runWith(Source.single(item), mat);
                });
    }

    // =======================
    // Fake API (for now)
    // =======================
    /**
     * Simulates an external API call (e.g., TMDb).
     *
     * @param query search query
     * @return list of fake results
     */
    private List<JsonNode> fakeApiCall(String query) {
        List<JsonNode> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ObjectNode json = Json.newObject();
            json.put("id", UUID.randomUUID().toString());
            json.put("title", query + " movie " + new Random().nextInt(100));
            list.add(json);
        }
        return list;
    }

    // =======================
    // Helper
    // =======================

    /**
     * Sends an error message to the client via the stream.
     *
     * @param msg error message
     */
    private void sendError(String msg) {
        ObjectNode error = Json.newObject();
        error.put("error", msg);
        hubSink.runWith(Source.single(error), mat);
    }
}