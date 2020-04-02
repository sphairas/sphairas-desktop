/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import org.thespheres.betula.admin.units.AbstractRemoteTargetsAction;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;

final class SetSigneeActionDelegate extends AbstractRemoteTargetsAction {

    private final Map<DocumentId, Signee> invalid = new HashMap<>();
    private final SigneeAction original;

    SetSigneeActionDelegate(Lookup context, SigneeAction original) {
        super(context, true);
        this.original = original;
        updateEnabled();
    }

    @Override
    protected AbstractRemoteTargetsAction createAbstractRemoteTargetsAction(Lookup context) {
        return new SetSigneeActionDelegate(context, original);
    }

    @Override
    public String getName() {
        try {
            term = findCommonTerm();
        } catch (IOException ex) {
            setEnabled(false);
        }
        return SetSigneeActionDelegate.class.getName();
    }

    @Override
    public void actionPerformed(final List<RemoteTargetAssessmentDocument> l, Optional<AbstractUnitOpenSupport> support) {
        //never happens
    }

    @Override
    protected void onContextChange(final List<RemoteTargetAssessmentDocument> all, Optional<AbstractUnitOpenSupport> support) {
        final SigneeSelection[] sel = new SigneeSelection[]{null};
        if (!all.isEmpty()) {
            support.flatMap(AbstractUnitOpenSupport::getSignees)
                    .ifPresent(signees -> {
                        final Signee current = extractSignee(all);
                        if (current == null || signees.getSigneeSet().contains(current)) {
                            sel[0] = new SigneeSelection(all, signees.getProviderUrl(), current, support.get());
                        }
                    });
        }
        original.updateComboBox(sel[0]);
    }

    private Signee extractSignee(final Collection<RemoteTargetAssessmentDocument> all) {
        final int s = all.size();
        final Signee current = all.stream()
                .flatMap(rtad -> rtad.getSignees().entrySet().stream())
                .filter(me -> me.getKey().equals(original.entitlement))
                .collect(Collectors.mapping(Map.Entry::getValue,
                        Collectors.collectingAndThen(Collectors.toList(), l -> {
                            if (l.size() == s) {
                                final Signee ret = l.get(0);
                                if (l.stream()
                                        .allMatch(sig -> sig.equals(ret))) { //TODO; NPE sig == null, happens after setting, resetting signee (concurrent mod??)
                                    return ret;
                                }
                            }
                            return (Signee) null;
                        })));
        return current;
    }

    static class InvalidHighlighter extends ColorHighlighter {

        InvalidHighlighter(Color cellBackground, Color cellForeground, Color selectedBackground, Color selectedForeground) {
            super(cellBackground, cellForeground, selectedBackground, selectedForeground);
        }

        private void updateState() {
            fireStateChanged();
        }

    }

    class SigneeSelection implements HighlightPredicate, PropertyChangeListener {

        private final Set<RemoteTargetAssessmentDocument> docs;
        final String signees;
        private Signee signee;
        private boolean invalidated;
        private final AbstractUnitOpenSupport support;
        private Signee userSelection;

        private SigneeSelection(List<RemoteTargetAssessmentDocument> docs, String signees, Signee current, AbstractUnitOpenSupport support) {
            this.docs = new HashSet<>(docs);
            this.signees = signees;
            this.signee = current == null ? Signee.NULL : current;
            this.support = support;
            updateInvalidated();
        }

        private void updateInvalidated() {
            invalidated = this.docs.stream()
                    .map(RemoteTargetAssessmentDocument::getDocumentId)
                    .anyMatch(invalid::containsKey);
            if (invalidated) {
                userSelection = this.docs.stream()
                        .filter(rtad -> invalid.containsKey(rtad.getDocumentId()))
                        .peek(rtad -> rtad.addPropertyChangeListener(this)) //TODO remove Listener
                        .map(rtad -> invalid.get(rtad.getDocumentId()))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
                            if (!l.isEmpty()) {
                                final Signee ret = l.get(0);
                                if (l.stream()
                                        .allMatch(sig -> Objects.equals(sig, ret))) { //TODO all sig can be null if signee is reset to null by user
                                    return ret;
                                }
                            }
                            return (Signee) null;
                        }));
            }
        }

        Signee getSelectedSignee() {
            return invalidated ? userSelection : signee;
        }

        @Messages({"SetSigneeActionDelegate.setSelectedSignee.message.exception=Ein Fehler beim Setzen von Unterzeichner {0}, Typ {1} für ist aufgetreten."})
        boolean setSelectedSignee(Signee sel) {
            if (!Objects.equals(sel, signee)) {
                docs.stream()
                        .map(RemoteTargetAssessmentDocument::getDocumentId)
                        .forEach(d -> invalid.put(d, sel));
                try {
                    if (!Objects.equals(sel, userSelection)) {
                        SigneeUpdateEdit undo = SigneeUpdateEdit.createAndRun(support, docs, original.entitlement, signee, sel);
                        original.getUndoSupport().postEdit(undo);
                        userSelection = sel;
                    }
                    return true;
                } catch (IOException ex) {
                    String msg = NbBundle.getMessage(SetSigneeActionDelegate.class, "SetSigneeActionDelegate.setSelectedSignee.message.exception", sel, original.entitlement, docs);
                    Logger.getLogger(SetSigneeActionDelegate.class.getName()).log(Level.SEVERE, msg, ex);
                    return false;
                } finally {
                    updateInvalidated();
                }
            } else {
                return true;
            }
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            return invalidated && adapter.getValue() != null && ((Signee) adapter.getValue()).equals(userSelection);
        }

        boolean isInvalidated() {
            return invalidated;
        }

        void removePCLs() {
            this.docs.stream()
                    .filter(rtad -> invalid.containsKey(rtad.getDocumentId()))
                    .forEach(rtad -> rtad.removePropertyChangeListener(this));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (RemoteTargetAssessmentDocument.PROP_SIGNEES.equals(evt.getPropertyName())) {
               final RemoteTargetAssessmentDocument source = (RemoteTargetAssessmentDocument) evt.getSource();
//                final Signee sig = source.getSignees().get(entitlement);
                invalid.remove(source.getDocumentId());
                updateInvalidated();
                if (!invalidated) {
                    signee = extractSignee(docs);
                    //Just in case. New signee should be userSelection already set.
                    original.signeeBox.setSelectedItem(signee);
                    //We need to explicitly remove the highlight
                    original.invalidatedHighlighter.updateState();
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Objects.hashCode(this.docs);
            return 79 * hash + Objects.hashCode(this.signees);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SigneeSelection other = (SigneeSelection) obj;
            if (!Objects.equals(this.docs, other.docs)) {
                return false;
            }
            return Objects.equals(this.signees, other.signees);
        }

    }
}
