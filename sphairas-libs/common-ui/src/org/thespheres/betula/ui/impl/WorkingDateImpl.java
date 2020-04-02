/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.event.ChangeListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.WorkingDate;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = WorkingDate.class)
public final class WorkingDateImpl implements WorkingDate {

    @Override
    public void addChangeListener(ChangeListener l) {
        getWorkingDateAction().cSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        getWorkingDateAction().cSupport.removeChangeListener(l);
    }

    @Deprecated
    @Override
    public Date getCurrentWorkingDate() {
        return getWorkingDateAction().getCurrentWorkingDate();
    }

    @Override
    public LocalDate getCurrentWorkingLocalDate() {
        final Date d = getCurrentWorkingDate();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public boolean isNow() {
        return getWorkingDateAction().isLinkDay();
    }

    @Override
    public Updater markUpdating() {
        return getWorkingDateAction().markUpdating();
    }

    private WorkingDateAction getWorkingDateAction() {
        return SystemAction.get(WorkingDateAction.class);
    }

}
