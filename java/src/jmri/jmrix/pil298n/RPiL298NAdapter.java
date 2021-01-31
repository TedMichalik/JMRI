package jmri.jmrix.pil298n;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * RPiL298N managers to be handled.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @author Paul Bender Copyright (C) 2015
 */
public class RPiL298NAdapter extends jmri.jmrix.AbstractPortController {

    // in theory gpio can be static, because there will only ever
    // be one, but the library handles the details that make it a
    // singleton.
    private GpioController gpio = null;

    public RPiL298NAdapter() {
        super(new RPiL298NSystemConnectionMemo());
        log.debug("RPiL298N GPIO Adapter Constructor called");
        this.manufacturerName = RPiL298NConnectionTypeList.PI;
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
    public RPiL298NSystemConnectionMemo getSystemConnectionMemo() {
        return (RPiL298NSystemConnectionMemo) super.getSystemConnectionMemo();
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

    private final static Logger log = LoggerFactory.getLogger(RPiL298NAdapter.class);

}
