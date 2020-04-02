/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 * @param <M>
 */
public abstract class MultiContextSensitiveAction<M extends MultiContextAction> extends AbstractAction {

    protected Lookup currentContext;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    protected MultiContextSensitiveAction() {
        updateEnabled(false, Lookup.EMPTY);
    }

    protected void updateEnabled(boolean enabled, final Lookup instances) {
        setEnabled(enabled);
        currentContext = instances;
    }

    protected final void updateName() {
        putValue(Action.NAME, getName(isEnabled()));
    }

    protected String getName(final boolean enabled) {
        return (String) getValue(Action.NAME);
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        actionPerformed(e, currentContext);
    }

    public abstract void actionPerformed(ActionEvent e, Lookup context);
}
