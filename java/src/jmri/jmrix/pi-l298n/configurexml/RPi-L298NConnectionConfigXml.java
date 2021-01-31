package jmri.jmrix.pi-l298n.configurexml;

import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.pi-l298n.RPi-L298NAdapter;
import jmri.jmrix.pi-l298n.RPi-L298NConnectionConfig;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of layout connections by persisting the
 * RPi-L298NAdapter. Note this is named as the XML version of a
 * RPi-L298NConnectionConfig object, but it's actually persisting the
 * RPi-L298NAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2015
 */
public class RPi-L298NConnectionConfigXml extends AbstractConnectionConfigXml {

    private RPi-L298NAdapter adapter = null;

    public RPi-L298NConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        log.debug("getInstance without Parameter called");
        if (adapter == null) {
            adapter = new RPi-L298NAdapter();
            if (adapter.getGPIOController() == null) {
                handleException("Not running on Raspberry PI.", null, adapter.getSystemPrefix(), adapter.getUserName(), null);
            }
        }
    }

    protected void getInstance(Object object) {
        log.debug("getInstance with Parameter called");
        adapter = ((RPi-L298NConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new RPi-L298NConnectionConfig(adapter));
    }

    /**
     * Default implementation for storing the static contents of the serial port
     * implementation
     *
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        getInstance(o);
        Element e = new Element("connection");
        storeCommon(e, adapter);
        e.setAttribute("class", this.getClass().getName());
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        getInstance();
        loadCommon(shared, perNode, adapter);

        // register, so can be picked up next time
        register();

        adapter.configure();
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(RPi-L298NConnectionConfigXml.class);

}
