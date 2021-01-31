package jmri.jmrix.pi-l298n;

import javax.annotation.Nonnull;
import jmri.Turnout;

/**
 * Implement Pi turnout manager.
 * <p>
 * System names are "PTnnn", where P is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author   Paul Bender Copyright (C) 2015
 */
public class RPi-L298NTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    // ctor has to register for RPi-L298N events
    public RPi-L298NTurnoutManager(RPi-L298NSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public RPi-L298NSystemConnectionMemo getMemo() {
        return (RPi-L298NSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(@Nonnull String systemName, String userName) {
        Turnout t = new RPi-L298NTurnout(systemName, userName);
        return t;
    }
    
    /**
     * Validates to Integer Format 0-999 with valid prefix.
     * eg. PT0 to PT999
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return this.validateIntegerSystemNameFormat(name, 0, 999, locale);
    }

}
