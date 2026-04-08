package actors.financialPerformance;

import actors.fpActors.FinancialPerformanceActor;
import actors.fpActors.FpResult;
import actors.fpActors.GetFPInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

public class FinancialPerformanceActorTest {

    /**
     * Shared Pekko TestKit resource for managing the actor system lifecycle.
     * This is initialized once for all tests in this class.
     */
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testGetFPInfoMessage() {
        TestProbe<FpResult> probe = testKit.createTestProbe();
        
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode mockMovie = mapper.createObjectNode();
        mockMovie.put("id", 42069);
        mockMovie.put("title", "Sharknado 35");
        mockMovie.put("budget", 1000);
        mockMovie.put("revenue", 150000);

        var readabilityActor = testKit.spawn(FinancialPerformanceActor.create());
        readabilityActor.tell(new GetFPInfo(mockMovie, probe.getRef()));
        FpResult result = probe.receiveMessage();

        // assertions
        assertEquals("Sharknado 35", result.title);
        assertEquals("149000", result.netProfit);
        assertEquals("14900.00", result.roi);
        assertEquals("Blockbuster Success", result.financialStatus);
    }

    @Test
    public void testGetFPInfoMessage_ZeroBudget() {
        TestProbe<FpResult> probe = testKit.createTestProbe();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode mockMovie = mapper.createObjectNode();
        mockMovie.put("id", 42069);
        mockMovie.put("title", "Sharknado 700");
        mockMovie.put("budget", 0);
        mockMovie.put("revenue", 150000);

        var readabilityActor = testKit.spawn(FinancialPerformanceActor.create());
        readabilityActor.tell(new GetFPInfo(mockMovie, probe.getRef()));
        FpResult result = probe.receiveMessage();

        // assertions
        assertEquals("Sharknado 700", result.title);
        assertEquals("150000", result.netProfit);
        assertEquals("Unknown; This movie has no recorded budget.", result.roi);
        assertEquals("Unknown", result.financialStatus);
    }
}
