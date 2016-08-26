package jmri.server.json.signalMast;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author Paul Bender
 * @author Randall Wood
 * @author Steve Todd
 */
public class JsonSignalMastSocketServiceTest extends TestCase {

    public void testCtorSuccess() {
        JsonSignalMastSocketService service = new JsonSignalMastSocketService(new JsonMockConnection((DataOutputStream) null));
        Assert.assertNotNull(service);
    }

    public void testSignalMastChange() {
        try {
            //create a signalmast for testing
            String sysName = "IF$shsm:basic:one-searchlight:SM2";
            String userName = "SM2";        
            SignalMastManager manager = InstanceManager.getDefault(SignalMastManager.class);
            SignalMast s = manager.provideSignalMast(sysName);
            s.setUserName(userName);

            JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
            JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
            JsonSignalMastSocketService service = new JsonSignalMastSocketService(connection);

            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, Locale.ENGLISH);
            // TODO: test that service is listener in SignalMastManager
            Assert.assertEquals(JSON.ASPECT_UNKNOWN, connection.getMessage().path(JSON.DATA).path(JSON.STATE).asText());

            //change to Approach, and wait for change to show up
            s.setAspect("Approach");
            JUnitUtil.waitFor(() -> {
                return s.getAspect().equals("Approach");
            }, "SignalMast is now Approach");
            Assert.assertEquals("Approach", connection.getMessage().path(JSON.DATA).path(JSON.STATE).asText());
            
            //change to Stop, and wait for change to show up
            s.setAspect("Stop");
            JUnitUtil.waitFor(() -> {
                return s.getAspect().equals("Stop");
            }, "SignalMast is now Stop");
            Assert.assertEquals("Stop", connection.getMessage().path(JSON.DATA).path(JSON.STATE).asText());
            
            service.onClose();
//            // TODO: test that service is no longer a listener in SignalMastManager
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    public void testOnMessageChange() {
        JsonNode message;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSignalMastSocketService service = new JsonSignalMastSocketService(connection);

        //create a signalmast for testing
        String sysName = "IF$shsm:basic:one-searchlight:SM2";
        String userName = "SM2";        
        SignalMastManager manager = InstanceManager.getDefault(SignalMastManager.class);
        SignalMast s = manager.provideSignalMast(sysName);
        s.setUserName(userName);

        try {
            // SignalMast Stop
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, "Stop");
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, Locale.ENGLISH);           
            Assert.assertEquals("Stop", s.getAspect()); //aspect should be Stop
        } catch (IOException | JmriException | JsonException ex) {
            Assert.fail(ex.getMessage());
        }

        // Try to set SignalMast to Unknown, should throw error, remain at Stop
        Exception exception = null;
        try {
            message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, userName).put(JSON.STATE, JSON.ASPECT_UNKNOWN);
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, Locale.ENGLISH);
            Assert.assertEquals("Stop", s.getAspect());
        } catch (IOException | JmriException | JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        
        // set SignalMast no value, should throw error, remain at Stop
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, sysName);
        exception = null;
        try {
            service.onMessage(JsonSignalMast.SIGNAL_MAST, message, Locale.ENGLISH);
        } catch (JsonException ex) {
            exception = ex;
        } catch (IOException | JmriException ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertNull(exception);
        Assert.assertEquals("Stop", s.getAspect());
    }

    // from here down is testing infrastructure
    public JsonSignalMastSocketServiceTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JsonSignalMastSocketServiceTest.class.getName()};
        TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JsonSignalMastSocketServiceTest.class);

        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        Log4JFixture.setUp();
        super.setUp();
        JUnitUtil.resetInstanceManager();
//        JUnitUtil.initInternalSignalMastManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        Log4JFixture.tearDown();
    }

}
