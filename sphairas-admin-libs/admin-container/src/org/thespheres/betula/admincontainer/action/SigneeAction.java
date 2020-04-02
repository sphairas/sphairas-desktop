/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.undo.UndoableEditSupport;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.openide.awt.UndoRedo;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.ui.swingx.SigneeConverter;

/**
 *
 * @author boris.heithecker
 */
public class SigneeAction extends AbstractAction implements Presenter.Toolbar {

    final String entitlement;
    final SelectionModel model = new SelectionModel();
    final JXComboBox signeeBox = new JXComboBox(model);
    final SetSigneeActionDelegate.InvalidHighlighter invalidatedHighlighter = new SetSigneeActionDelegate.InvalidHighlighter(Color.PINK, null, Color.MAGENTA, null);
    SetSigneeActionDelegate.SigneeSelection currentSelection;
    final Listener listener;
    protected UndoableEditSupport undoSupport;
    //    final BasicTextUI.BasicHighlighter highlighter;
    private final ContextSigneeConverter stringValue = new ContextSigneeConverter();

    public static SigneeAction create(String entitlement) {
        return new SigneeAction(entitlement);
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    private SigneeAction(String entitlement) {
        this.entitlement = entitlement;
        putValue("org.thespheres.betula.admincontainer.action.SigneeAction.entitlement", entitlement);
        listener = new Listener();
        signeeBox.addHighlighter(invalidatedHighlighter);
        //Don't use autocompletion, will fire to many events.....
        //            AutoCompleteDecorator.decorate(signeeBox, currentSelection.sigConv);
        signeeBox.setRenderer(new DefaultListRenderer(stringValue));
//        highlighter = new BasicTextUI.BasicHighlighter();
//        ((JTextField) signeeBox.getEditor().getEditorComponent()).setHighlighter(highlighter);
    }

    @Override
    public boolean isEnabled() {
        return listener != null && listener.isEnabled();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Must never be called.");
    }

    @Override
    public Component getToolbarPresenter() {
        return signeeBox;
    }

    void updateComboBox(final SetSigneeActionDelegate.SigneeSelection selection) {
        if (selection != null && !Objects.equals(currentSelection, selection)) {
            boolean updateSignees = true;
            if (currentSelection != null) {
                currentSelection.removePCLs();
                updateSignees = !currentSelection.signees.equals(selection.signees);
            }
            currentSelection = selection;
            if (updateSignees) {
                model.update();
            } else {
                model.updateSignee();
            }
            invalidatedHighlighter.setHighlightPredicate(currentSelection);
        } else if (selection == null) {
            if (currentSelection != null) {
                currentSelection.removePCLs();
            }
            currentSelection = null;
            model.update();
        } else {
            model.updateSignee();
        }
        signeeBox.setEnabled(isEnabled());
    }

    UndoableEditSupport getUndoSupport() {
        if (undoSupport == null) {
            undoSupport = new UndoableEditSupport(this);
            Component comp = signeeBox;
            while ((comp = comp.getParent()) != null) {
                if (comp instanceof TopComponent) {
                    UndoRedo.Manager ur = (UndoRedo.Manager) ((TopComponent) comp).getUndoRedo();
                    undoSupport.addUndoableEditListener(ur);
                }
            }
        }
        return undoSupport;
    }

    private class ContextSigneeConverter extends SigneeConverter {

        public ContextSigneeConverter() {
            super(null);
        }

        @Override
        protected Optional<Signees> getSignees() {
            return currentSelection != null ? Signees.get(currentSelection.signees) : Optional.empty();
        }

        @Override
        protected String nullReturn(Object v) {
            return "---";
        }

        @Override
        protected BiFunction<Signee, String, String> after() {
            if (currentSelection == null) {
                return super.after();
            }
            return (sig, sn) -> sn + (currentSelection.isInvalidated() ? "*" : "");
        }

    }

    class SelectionModel extends DefaultComboBoxModel<Signee> {

        private boolean updating;

        private void update() {
            updating = true;
            removeAllElements();
            if (currentSelection != null) {
                addElement(Signee.NULL);
                Signees.get(currentSelection.signees)
                        .map(Signees::getSigneeSet)
                        .ifPresent(s -> s.forEach(this::addElement));
            }
            updating = false;
            updateSignee();
        }

        private void updateSignee() {
            if (currentSelection != null) {
                super.setSelectedItem(currentSelection.getSelectedSignee());
            }
        }

        @Override
        public void setSelectedItem(Object value) {
            if (updating) {
                return;
            }
            if (currentSelection != null && currentSelection.setSelectedSignee((Signee) value)) {
                super.setSelectedItem(value);
            }
        }
    }

    private class Listener implements PropertyChangeListener {

        private final Action contextAction;

        @SuppressWarnings("LeakingThisInConstructor")
        private Listener() {
            this.contextAction = new SetSigneeActionDelegate(Utilities.actionsGlobalContext(), SigneeAction.this);
            this.contextAction.addPropertyChangeListener(this);
        }

        private boolean isEnabled() {
            return contextAction.isEnabled();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case Action.NAME:
                    SigneeAction.this.putValue(Action.NAME, contextAction.getValue(Action.NAME));
                    break;
                case "enabled":
                    SigneeAction.this.setEnabled(contextAction.isEnabled());
                    break;
            }
        }

    }
}
