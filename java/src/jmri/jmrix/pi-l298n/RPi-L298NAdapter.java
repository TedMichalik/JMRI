package jmri.jmrix.pi-l298n;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * RPi-L298N managers to be handled.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Paul Bender Copyright (C) 2015
 */
public class RPi-L298NAdapter extends jmri.jmrix.AbstractPortController {

    // in theory gpio can be static, because there will only ever
    // be one, but the library handles the details that make it a
    // singleton.
    private GpioController gpio = null;

    public RPi-L298NAdapter() {
        super(new RPi-L298NSystemConnectionMemo());
        log.debug("RPi-L298N GPIO Adapter Constructor called");
        this.manufacturerName = RPi-L298NConnectionTypeList.PI;
        try {
            gpio = GpioFactory.getInstance();
            opened = true;
        } catch (UnsatisfiedLinkError er) {
            log.error("Expected to run on Raspberry PI, but does not appear to be.");
        }
    }

    @Override
    public String getCurrentPortName() {
        return "GPIO";
    }

    @Override
    public void dispose() {
        super.dispose();
        gpio.shutdown(); // terminate all GPIO connections.
    }

    @Override
    public void connect() {
    }

    @Override
    public void configure() {
        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public java.io.DataInputStream getInputStream() {
        return null;
    }

    @Override
    public java.io.DataOutputStream getOutputStream() {
        return null;
    }

    @Override
    public RPi-L298NSystemConnectionMemo getSystemConnectionMemo() {
        return (RPi-L298NSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void recover() {
    }

    /*
    * Get the GPIO Controller associated with this object.
    *
    * @return the associaed GPIO Controller or null if none exists
     */
    @CheckForNull
    public GpioController getGPIOController() {
        return gpio;
    }

    private final static Logger log = LoggerFactory.getLogger(RPi-L298NAdapter.class);

}
