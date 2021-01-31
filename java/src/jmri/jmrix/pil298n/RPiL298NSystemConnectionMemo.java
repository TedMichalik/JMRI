package jmri.jmrix.pil298n;

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
public class RPiL298NSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public RPiL298NSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name); // NOI18N

        InstanceManager.store(this, RPiL298NSystemConnectionMemo.class);
        log.debug("Created RPiL298NSystemConnectionMemo");
    }

    public RPiL298NSystemConnectionMemo(){
        this("P", "RPiL298N");
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
       setTurnoutManager(new RPiL298NTurnoutManager(this));
       setSensorManager(new RPiL298NSensorManager(this));
       register();
    }
    
    @Override
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.pil298n.RPiL298NActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, RPiL298NSystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(RPiL298NSystemConnectionMemo.class);

}
