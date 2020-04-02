/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.openide.util.Lookup;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.validation.ValidationResult;

/**
 *
 * @author boris.heithecker
 * @param <R>
 */
public abstract class RemoteTargetDocumentCellValidation<R extends ValidationResult> extends RemoteUnitsCellValidation<R> {

    protected RemoteTargetDocumentCellValidation(String iconBase, Lookup context) {
        super(iconBase, context);
    }

    @Override
    public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
       final int ci = adapter.convertColumnIndexToModel(adapter.column);
        if (ci > 0 && ci < adapter.getColumnCount()) {
            final Object cio = adapter.getColumnIdentifierAt(ci);
            final Object s = adapter.getValue(0);
            final TermId term = findCurrentTerm(adapter);
            if (term != null && cio instanceof DocumentId && s instanceof RemoteStudent) {
                final DocumentId document = (DocumentId) cio;
                final RemoteStudent student = (RemoteStudent) s;
                return isHighlighted(student, term, document);
            }
        }
        return false;
    }

    private TermId findCurrentTerm(final ComponentAdapter adapter) {
        final JComponent cmp = adapter.getComponent();
        if (cmp instanceof JXTable) {
            final Object prop = cmp.getClientProperty("current.term.id");
            final Object listener = cmp.getClientProperty(Listener.class.getName());
            if (listener == null) {
                final Listener li = new Listener(this);
                cmp.addPropertyChangeListener(li);
                cmp.putClientProperty(Listener.class.getName(), listener);
            }
            if (prop instanceof TermId) {
                return (TermId) prop;
            }
        }
        return null;
    }

    //Prevent IllegalAccessEx, BootstrapError
    void stateChanged() {
        fireStateChanged();
    }

    static class Listener implements PropertyChangeListener {

        private final WeakReference<RemoteTargetDocumentCellValidation> ref;

        public Listener(RemoteTargetDocumentCellValidation cv) {
            this.ref = new WeakReference<>(cv);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() instanceof JComponent) {
                JComponent source = (JComponent) evt.getSource();
                RemoteTargetDocumentCellValidation cv = ref.get();
                if (cv != null) {
                    if ("current.term.id".equals(evt.getPropertyName())) {
                        EventQueue.invokeLater(cv::stateChanged);
                    }
                } else {
                    source.removePropertyChangeListener(this);
                }
            }
        }

    }

    protected abstract boolean isHighlighted(RemoteStudent student, TermId term, DocumentId document);
}
