package jmri.jmrix.pi-l298n.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for
 * configuring RPi-L298NTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class RPi-L298NTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public RPi-L298NTurnoutManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.pi-l298n.configurexml.RPi-L298NTurnoutManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

//    private final static Logger log = LoggerFactory.getLogger(RPi-L298NTurnoutManagerXml.class);

}
