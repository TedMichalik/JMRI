package jmri.jmrix.pi-l298n;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid Raspberry Pi Connection Types
 *
 * @author Paul Bender Copyright (C) 2015
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class RPi-L298NConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String PI = "Raspberry Pi Foundation";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.pi-l298n.RPi-L298NConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{PI};
    }

}
