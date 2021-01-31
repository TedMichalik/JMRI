package jmri.jmrix.pi-l298n;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered in the 
 * instance manager to activate their particular system.
 *
 * @author   Paul Bender Copyright (C) 2015
 */
public class RPi-L298NSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public RPi-L298NSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name); // NOI18N

        InstanceManager.store(this, RPi-L298NSystemConnectionMemo.class);
        log.debug("Created RPi-L298NSystemConnectionMemo");
    }

    public RPi-L298NSystemConnectionMemo(){
        this("P", "RPi-L298N");
    }

    /*
     * Provides access to the SensorManager for this particular connection.
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager(){
        return get(SensorManager.class);

    }
    public void setSensorManager(SensorManager s){
         InstanceManager.setSensorManager(s);
         store(s,SensorManager.class);
    }

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager(){
        return get(TurnoutManager.class);
    }

    public void setTurnoutManager(TurnoutManager t){
         InstanceManager.setTurnoutManager(t);
         store(t,TurnoutManager.class);
    }

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager(){
        return get(LightManager.class);

    }
    public void setLightManager(LightManager l){
         InstanceManager.setLightManager(l);
         store(l,LightManager.class);
    }

    public void configureManagers(){
       setTurnoutManager(new RPi-L298NTurnoutManager(this));
       setSensorManager(new RPi-L298NSensorManager(this));
       register();
    }
    
    @Override
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.pi-l298n.RPi-L298NActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, RPi-L298NSystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(RPi-L298NSystemConnectionMemo.class);

}
