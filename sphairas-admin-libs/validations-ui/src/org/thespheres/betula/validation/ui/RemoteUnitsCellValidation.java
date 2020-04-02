/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.ui;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.validation.ValidationResult;
import org.thespheres.betula.validation.ValidationResultSet;

/**
 *
 * @author boris.heithecker
 * @param <R>
 */
public abstract class RemoteUnitsCellValidation<R extends ValidationResult> extends CellIconHighlighterValidation<PrimaryUnitOpenSupport, RemoteUnitsModel, R> {

//    private final Listener listener = new Listener();
    protected RemoteUnitsCellValidation(String iconBase, Lookup context) {
        super(iconBase, PrimaryUnitOpenSupport.class, context);
    }

    @Override
    protected void init() {
        super.init();
        if (support == null) {
            return;
        }
        final RemoteUnitsModel model;
        try {
            model = support.getRemoteUnitsModel();
        } catch (IOException ex) {
            Logger.getLogger(RemoteUnitsCellValidation.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
            return;
        }
        initializeValidation(model);
    }

    protected void initializeValidation(final RemoteUnitsModel model) {
        final ValidationResultSet<RemoteUnitsModel, R> v = createValidation(model);
        setValidation(v);
        change();
    }

    protected abstract ValidationResultSet<RemoteUnitsModel, R> createValidation(RemoteUnitsModel model);

    @Override
    public void onStart(int size, Cancellable cancel) {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void resultAdded(R result) {
        change();
    }

    @Override
    public void resultRemoved(R result) {
        change();
    }

    private void change() {
        EventQueue.invokeLater(this::fireStateChanged);
    }

//    private class Listener implements PropertyChangeListener {
//
//        @Override
//        public void propertyChange(PropertyChangeEvent evt) {
//            final String n = evt.getPropertyName();
//            if (RemoteUnitsModel.PROP_STUDENTS.equals(n) || RemoteUnitsModel.PROP_TARGETS.equals(n)) {
//                RP.post(RemoteUnitsCellValidation.this::initAll);
//            } else if (RemoteUnitsModel.PROP_INITIALISATION.equals(evt.getPropertyName())) {
//                RP.post(RemoteUnitsCellValidation.this::initAll);
//            }
//        }
//
//    }
}
