package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018 2019
 */
public class CbusMessageTest {

    @Test
    public void testCTor() {
        CbusMessage t = new CbusMessage();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testOpcRangeToSTL() {
        CanReply r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x93); // ARON OPC
        CanReply m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("ARON OPC Changed", m.getElement(0) == 0x90); // ACON OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x94); // AROF OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("AROF OPC Changed", m.getElement(0) == 0x91); // ACOF OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x9d); // ARSON OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("ARSON OPC Changed", m.getElement(0) == 0x98); // ASON OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x9e); // ARSOF OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("ARSOF OPC Changed", m.getElement(0) == 0x99); // ASOF OPC
        
        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x95); // EVULN OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("Other OPCs do not change", m.getElement(0) == 0x95); // EVULN OPC
    }    
    
    @Test
    public void testgetNodeNumberMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xee);
        m.setElement(2, 0x56);
        m.setElement(3, 0x11);
        m.setElement(4, 0x16);
        Assert.assertTrue("Node calculated OK", CbusMessage.getNodeNumber(m) == 61014);
        m.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getNodeNumber(m) == 0 );
    }
    
    @Test
    public void testgetNodeNumberReply() {
        CanReply r = new CanReply();
        r.setNumDataElements(5);
        r.setElement(0, 0x90); // ACON OPC
        r.setElement(1, 0xee);
        r.setElement(2, 0x56);
        r.setElement(3, 0x11);
        r.setElement(4, 0x16);
        Assert.assertTrue("Node calculated OK", CbusMessage.getNodeNumber(r) == 61014);
        r.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getNodeNumber(r) == 0 );
    }

    @Test
    public void testgetEventMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xee);
        m.setElement(2, 0x56);
        m.setElement(3, 0x11);
        m.setElement(4, 0x16);
        Assert.assertTrue("Event calculated OK", CbusMessage.getEvent(m) == 4374);
        m.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getEvent(m) == 0 );
    }
    
    @Test
    public void testgetEventReply() {
        CanReply r = new CanReply();
        r.setNumDataElements(5);
        r.setElement(0, 0x90); // ACON OPC
        r.setElement(1, 0xee);
        r.setElement(2, 0x56);
        r.setElement(3, 0x11);
        r.setElement(4, 0x16);
        Assert.assertTrue("Event calculated OK", CbusMessage.getEvent(r) == 4374);
        r.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getEvent(r) == 0 );
    }

    @Test
    public void testgetEventTypeMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Event Type On", CbusMessage.getEventType(m) == CbusConstants.EVENT_ON);
        m.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Event Type Off", CbusMessage.getEventType(m) == CbusConstants.EVENT_OFF );
    }

    @Test
    public void testgetEventTypeReply() {
        CanReply r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Event Type On", CbusMessage.getEventType(r) == CbusConstants.EVENT_ON);
        r.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Event Type Off", CbusMessage.getEventType(r) == CbusConstants.EVENT_OFF );
    }

    @Test
    public void testisEventMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Event", CbusMessage.isEvent(m) == true);
        m.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Is Not Event", CbusMessage.isEvent(m) == false );
    }

    @Test
    public void testisEventReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Event", CbusMessage.isEvent(r) == true);
        r.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Is Not Event", CbusMessage.isEvent(r) == false );
    }

    @Test
    public void testisShortMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Not Short", CbusMessage.isShort(m) == false);
        m.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Is Short", CbusMessage.isShort(m) == true );
    }

    @Test
    public void testisShortReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Not Short", CbusMessage.isShort(r) == false);
        r.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Is Short", CbusMessage.isShort(r) == true );
    }

    @Test
    public void testtoAddressMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xdd);
        m.setElement(2, 0xab);
        m.setElement(3, 0x4b);
        m.setElement(4, 0xb3);

        Assert.assertEquals("string toAddressMessageAcon", CbusMessage.toAddress(m),"+n56747e19379");
        m.setElement(0, 0x91); // ACOF OPC
        Assert.assertEquals("toAddressMessageAcof", CbusMessage.toAddress(m),"-n56747e19379" );
        m.setElement(0, 0x98); // ASON OPC
        Assert.assertEquals("toAddressMessageAson", CbusMessage.toAddress(m),"+19379" );
        m.setElement(0, 0x99); // ASOF OPC
        Assert.assertEquals("toAddressMessageAsof", CbusMessage.toAddress(m),"-19379" );
        m.setElement(0, 0x9e); // ARSON OPC
        Assert.assertEquals("toAddressMessageArson", CbusMessage.toAddress(m),"X9EDDAB4BB3" );
    }
    
    @Test
    public void testtoAddressReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(5);
        r.setElement(0, 0x90); // ACON OPC
        r.setElement(1, 0xdd);
        r.setElement(2, 0xab);
        r.setElement(3, 0x4b);
        r.setElement(4, 0xb3);

        Assert.assertEquals("toAddressReplyAcon", CbusMessage.toAddress(r),"+n56747e19379");
        r.setElement(0, 0x91); // ACOF OPC
        Assert.assertEquals("toAddressReplyAcof", CbusMessage.toAddress(r),"-n56747e19379" );
        r.setElement(0, 0x98); // ASON OPC
        Assert.assertEquals("toAddressReplyAson", CbusMessage.toAddress(r),"+19379" );
        r.setElement(0, 0x99); // ASOF OPC
        Assert.assertEquals("toAddressReplyAsof", CbusMessage.toAddress(r),"-19379" );
        r.setElement(0, 0x9e); // ARSON OPC
        Assert.assertEquals("toAddressReplyArson", CbusMessage.toAddress(r),"X9EDDAB4BB3" );
    }    
    
    @Test
    public void testisRequestTrackOffMessage() {
        CanMessage m = new CanMessage(0x12,1);
        m.setElement(0, 0x08); // RTOF OPC
        Assert.assertEquals("isRequestTrackOff Good Message", CbusMessage.isRequestTrackOff(m),true);
        m = new CanMessage(0x12,1);
        m.setElement(0, 0x09); // RTON OPC    
        Assert.assertEquals("isRequestTrackOff Bad Message", CbusMessage.isRequestTrackOff(m),false);
    }
    
    @Test
    public void testisRequestTrackOnMessage() {
        CanMessage m = new CanMessage(0x12,1);
        m.setElement(0, 0x09); // RTON OPC
        Assert.assertEquals("isRequestTrackOn Good Message", CbusMessage.isRequestTrackOn(m),true);
        m = new CanMessage(0x12,1);
        m.setElement(0, 0x08); // RTOF OPC    
        Assert.assertEquals("isRequestTrackOn Bad Message", CbusMessage.isRequestTrackOn(m),false);
    }

    @Test
    public void testisTrackOnReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x05); // TON OPC
        Assert.assertEquals("isRequestTrackOn Good Reply", CbusMessage.isTrackOn(r),true);
        r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x04); // TOF OPC    
        Assert.assertEquals("isRequestTrackOn Bad Reply", CbusMessage.isTrackOn(r),false);
    }

    @Test
    public void testisTrackOffReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x04); // TOF OPC
        Assert.assertEquals("isRequestTrackOff Good Reply", CbusMessage.isTrackOff(r),true);
        r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x05); // TON OPC    
        Assert.assertEquals("isRequestTrackOff Bad Reply", CbusMessage.isTrackOff(r),false);
    }
    
    @Test
    public void testgetRequestTrackOnMessage() {
        CanMessage m = CbusMessage.getRequestTrackOn(0x12);
        Assert.assertTrue("getRequestTrackOn OPC", m.getElement(0) == 0x09); // RTON OPC
        Assert.assertTrue("getRequestTrackOn Length", m.getNumDataElements() == 1);
    }
    
    @Test
    public void testgetRequestTrackOffMessage() {
        CanMessage m = CbusMessage.getRequestTrackOff(0x12);
        Assert.assertTrue("getRequestTrackOff OPC", m.getElement(0) == 0x08); // RTON OPC
        Assert.assertTrue("getRequestTrackOff Length", m.getNumDataElements() == 1);
    }

    @Test
    public void testgetDataLength() {
        CanReply r = new CanReply(0x12);
        CanMessage m = new CanMessage(0x12);
        r.setElement(0, 0x04); // TOF OPC
        m.setElement(0, 0x04); // TOF OPC
        Assert.assertEquals("Data Length 0 r",0,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 0 m",0,CbusMessage.getDataLength(m));
        r.setElement(0, 0x11); // RQMN
        m.setElement(0, 0x11); // RQMN
        Assert.assertEquals("Data Length 0 r",0,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 0 m",0,CbusMessage.getDataLength(m));

        r.setElement(0, 0x83); // WCVB OPC
        m.setElement(0, 0x83); // WCVB OPC
        Assert.assertEquals("Data Length 4 r",4,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 4 m",4,CbusMessage.getDataLength(m));
        r.setElement(0, 0xe2); // NAME
        m.setElement(0, 0xe2); // NAME
        Assert.assertEquals("Data Length 7 r",7,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 7 m",7,CbusMessage.getDataLength(m));
        
        r = null;
        m = null;
    }
    
    @Test
    public void testsetgetPriority() {
        CanReply r = new CanReply(0x00);
        CanMessage m = new CanMessage(0x00);
        Assert.assertEquals("Priority 0 r",0,CbusMessage.getPri(r));
        Assert.assertEquals("Priority 0 m",0,CbusMessage.getPri(m));
        
        try {
            CbusMessage.setPri(r,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        
        try {
            CbusMessage.setPri(m,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        
        CbusMessage.setPri(m,CbusConstants.DEFAULT_MINOR_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_MINOR_PRIORITY);
        Assert.assertEquals("Priority DEFAULT_MINOR_PRIORITY r",3,CbusMessage.getPri(r));
        Assert.assertEquals("Priority DEFAULT_MINOR_PRIORITY m",3,CbusMessage.getPri(m));

        CbusMessage.setPri(m,CbusConstants.DEFAULT_DYNAMIC_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_DYNAMIC_PRIORITY);
        Assert.assertEquals("Priority DEFAULT_DYNAMIC_PRIORITY r",2,CbusMessage.getPri(r));
        Assert.assertEquals("Priority DEFAULT_DYNAMIC_PRIORITY m",2,CbusMessage.getPri(m));
        
        r.setExtended(true);
        m.setExtended(true);
        
        Assert.assertEquals("Priority setExtended r",0,CbusMessage.getPri(r));
        Assert.assertEquals("Priority setExtended m",0,CbusMessage.getPri(m));        

        CbusMessage.setPri(m,CbusConstants.DEFAULT_MINOR_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_MINOR_PRIORITY);
        Assert.assertEquals("Priority setExtended DEFAULT_MINOR_PRIORITY r",3,CbusMessage.getPri(r));
        Assert.assertEquals("Priority setExtended DEFAULT_MINOR_PRIORITY m",3,CbusMessage.getPri(m));
        
    }
    
    @Test
    public void testsetgetId() {
        
        CanReply r = new CanReply(0x00);
        CanMessage m = new CanMessage(0x00);
        Assert.assertEquals("getId 0 r",0,CbusMessage.getId(r));
        Assert.assertEquals("getId 0 m",0,CbusMessage.getId(m));
        CbusMessage.setId(r,0x01);
        CbusMessage.setId(m,0x01);
        Assert.assertEquals("getId 1 r",1,CbusMessage.getId(r));
        Assert.assertEquals("getId 1 m",1,CbusMessage.getId(m));
        CbusMessage.setId(r,120);
        CbusMessage.setId(m,120);
        Assert.assertEquals("getId 120 r",120,CbusMessage.getId(r));
        Assert.assertEquals("getId 120 m",120,CbusMessage.getId(m));
        try {
            CbusMessage.setId(r,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        
        try {
            CbusMessage.setId(m,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        
        r.setExtended(true);
        m.setExtended(true);        
        
        CbusMessage.setId(r,0x01);
        CbusMessage.setId(m,0x01);
        Assert.assertEquals("getId 1 r",1,CbusMessage.getId(r));
        Assert.assertEquals("getId 1 m",1,CbusMessage.getId(m));
        CbusMessage.setId(r,120);
        CbusMessage.setId(m,120);
        Assert.assertEquals("getId 120 r",120,CbusMessage.getId(r));
        Assert.assertEquals("getId 120 m",120,CbusMessage.getId(m));        
        try {
            CbusMessage.setId(r,0xffffff);
            Assert.fail("r Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        
        try {
            CbusMessage.setId(m,0xffffff);
            Assert.fail("m Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }        
        
    }
    
    @Test
    public void testisArst() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x07); // Arst OPC
        Assert.assertTrue(CbusMessage.isArst(r));
        r.setElement(0, 0x06);
        Assert.assertFalse(CbusMessage.isArst(r));
    }
    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusMessageTest.class);

}
