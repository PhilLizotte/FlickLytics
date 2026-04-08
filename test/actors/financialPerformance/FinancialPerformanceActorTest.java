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
        FpResult result1 = probe.receiveMessage();

        mockMovie.put("revenue", 4000);
        readabilityActor.tell(new GetFPInfo(mockMovie, probe.getRef()));
        FpResult result2 = probe.receiveMessage();

        mockMovie.put("revenue", 2000);
        readabilityActor.tell(new GetFPInfo(mockMovie, probe.getRef()));
        FpResult result3 = probe.receiveMessage();

        mockMovie.put("revenue", 500);
        readabilityActor.tell(new GetFPInfo(mockMovie, probe.getRef()));
        FpResult result4 = probe.receiveMessage();
        
        // assertions
        assertEquals("Sharknado 35", result1.title);
        assertEquals("149000", result1.netProfit);
        assertEquals("14900.00", result1.roi);
        assertEquals("Blockbuster Success", result1.financialStatus);
        
        assertEquals("3000", result2.netProfit);
        assertEquals("300.00", result2.roi);
        assertEquals("High Return", result2.financialStatus);

        assertEquals("1000", result3.netProfit);
        assertEquals("100.00", result3.roi);
        assertEquals("Profitable", result3.financialStatus);

        assertEquals("-500", result4.netProfit);
        assertEquals("-50.00", result4.roi);
        assertEquals("Financial Loss", result4.financialStatus);
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
