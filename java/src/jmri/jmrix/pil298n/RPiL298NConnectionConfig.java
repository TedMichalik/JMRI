package jmri.jmrix.pil298n;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuring a Raspberry Pi layout connection.
 * <p>
 * This uses the {@link RPiL298NAdapter} class to do the actual connection.
 *
 * @author Paul Bender Copyright (C) 2015
 *
 * @see RPiL298NAdapter
 */
public class RPiL298NConnectionConfig extends jmri.jmrix.AbstractConnectionConfig {

    private boolean disabled = false;
    private RPiL298NAdapter adapter = null;
    private Date GPIOMessageShown = null;

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p the pre-existing adapter
     */
    public RPiL298NConnectionConfig(RPiL298NAdapter p) {
        super();
        adapter = p;
    }

    /**
     * Ctor for a connection configuration with no preexisting adapter.
     * {@link #setInstance()} will fill the adapter member.
     */
    public RPiL298NConnectionConfig() {
        super();
        adapter = new RPiL298NAdapter();
    }

    protected boolean init = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        log.debug("init called for {}", name());
        if (init) {
            return;
        }
        addNameEntryCheckers(adapter);
        init = true;
    }

    @Override
    public void updateAdapter() {
        if (adapter.getSystemConnectionMemo() != null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    @Override
    protected void showAdvancedItems() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(final javax.swing.JPanel details) {
        _details = details;
        setInstance();
        if (!init) {
            if (adapter.getSystemConnectionMemo() != null) {
                systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                NUMOPTIONS = NUMOPTIONS + 2;
            }
            addStandardDetails(adapter, false, NUMOPTIONS);
            init = false;
            checkInitDone();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new RPiL298NAdapter();
        }
        if (adapter.getGPIOController() == null) {
            // don't show more than once every 30 seconds
            if (!GraphicsEnvironment.isHeadless()
                    && (this.GPIOMessageShown == null || ((new Date().getTime() - this.GPIOMessageShown.getTime()) / 1000 % 60) > 30)) {
                JOptionPane.showMessageDialog(this._details,
                        Bundle.getMessage("NoGpioControllerMessage"),
                        Bundle.getMessage("NoGpioControllerTitle"),
                        JOptionPane.ERROR_MESSAGE);
                this.GPIOMessageShown = new Date();
            }
        }
    }

    @Override
    public RPiL298NAdapter getAdapter() {
        return adapter;
    }

    @Override
    public String getInfo() {
        return "GPIO";
    }

    String manuf = RPiL298NConnectionTypeList.PI;

    @Override
    public String getManufacturer() {
        return manuf;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        manuf = manufacturer;
    }

    @Override
    public String name() {
        return getConnectionName();
    }

    @Override
    public String getConnectionName() {
        return "Raspberry Pi GPIO"; // NOI18N
    }

    @Override
    public boolean getDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disable) {
        this.disabled = disable;
    }

    private final static Logger log = LoggerFactory.getLogger(RPiL298NConnectionConfig.class);

}
