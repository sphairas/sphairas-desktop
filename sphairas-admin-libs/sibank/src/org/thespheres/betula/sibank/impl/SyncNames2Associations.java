/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.sibank.SchuelerAssoziation;
import org.thespheres.betula.sibank.SiBankAssoziationenCollection;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;

/**
 *
 * @author boris.heithecker
 */
@Deprecated//This is useful only if StudentId is not presensent in import items  may be removed
@Messages({"SyncNames2Associations.message=<html><body>Für die Schülerin/den Schüler <br>{0}<br>Geburtsdatum: {1}<br>gibt es mehrere Zuordnungen in der Schülernamen-Datenbank.<br>Welche Zuordnung soll verwendet werden?</body></html>",
    "SyncNames2Associations.title=SiBank-Schülerzordnung wählen"})
class SyncNames2Associations {

    static Optional<StudentId> tryResolve(final ImportStudentKey key, final List<VCardStudent> select, final SiBankImportData<SiBankKursItem> wizard) {
        final SyncNames2AssociationsDialog panel = new SyncNames2AssociationsDialog(key, select);
        final Optional[] found = new Optional[]{Optional.empty()};
        final DialogDescriptor dd = new DialogDescriptor(
                panel, NbBundle.getMessage(SyncNames2Associations.class, "SyncNames2Associations.title"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        class PCL implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (AbstractFileImportAction.SOURCE_TARGET_LINKS.equals(evt.getPropertyName())) {
                    enable();
                }
            }

            private void enable() {
                final SiBankAssoziationenCollection links = (SiBankAssoziationenCollection) wizard.getProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS);
                final boolean enabled = links != null;
                if (!enabled) {
                    panel.saveCheckBox.setSelected(false);
                } else {
                    panel.saveCheckBox.setEnabled(enabled);
                    final Optional<StudentId> res = links.getSchuelerAssoziationen().stream()
                            .filter(s -> s.getKey().equals(key))
                            .collect(CollectionUtil.requireSingleton())
                            .map(a -> a.getStudent());
                    if (res.isPresent()) {
                        found[0] = res;
                        if (dialog.isVisible()) {
                            dialog.setVisible(false);
                        }
                    }
                }
            }
        }
        final PCL listener = new PCL();
        listener.enable();
        if (found[0].isPresent()) {
            return found[0];
        }
        wizard.addPropertyChangeListener(listener);
        dialog.setVisible(true);
        wizard.removePropertyChangeListener(listener);
        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            final boolean save = panel.saveCheckBox.isSelected() && panel.saveCheckBox.isEnabled();
            final StudentId selected = (StudentId) panel.model.getSelectedItem();
            if (save) {
                final SiBankAssoziationenCollection ll = (SiBankAssoziationenCollection) wizard.getProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS);
                final SchuelerAssoziation sa = new SchuelerAssoziation(key, selected);
                if (selected != null) {
                    ll.getSchuelerAssoziationen().add(sa);
                } else {
                    ll.getSchuelerAssoziationen().remove(sa);
                }
            }
            return Optional.ofNullable(selected);
        }
        return found[0];
    }

}
