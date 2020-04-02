/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.admincontainer.action.EditRemoteTargetMarkersEdit.MarkerSelection;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"EditRemoteTargetMarkersVisualPanel.step.name=Auswahl"})
class EditRemoteTargetMarkersVisualPanel extends JPanel {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final EditRemoteTargetMarkersTableModel model = new EditRemoteTargetMarkersTableModel();

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    EditRemoteTargetMarkersVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(scrollPanel, BorderLayout.CENTER);
        table.setColumnFactory(model.createColumnFactory());
        table.setModel(model);
    }

    void initialize(EditRemoteTargetMarkersEdit wiz) {
        model.initialize(wiz, Lookup.EMPTY);
    }

    void storeSelection(EditRemoteTargetMarkersEdit wiz) {
        final MarkerSelection[] selection = model.getSelection();
        wiz.setSelection(selection);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EditRemoteTargetMarkersVisualPanel.class, "EditRemoteTargetMarkersVisualPanel.step.name");
    }

    public static class EditRemoteTargetMarkersPanel implements WizardDescriptor.Panel<EditRemoteTargetMarkersEdit> {

        private EditRemoteTargetMarkersVisualPanel component;

        @Override
        public EditRemoteTargetMarkersVisualPanel getComponent() {
            if (component == null) {
                component = new EditRemoteTargetMarkersVisualPanel();
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            // Show no Help button for this panel:
            return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx("help.key.here");
        }

        @Override
        public boolean isValid() {
            // If it is always OK to press Next or Finish, then:
            return true;
            // If it depends on some condition (form filled out...) and
            // this condition changes (last form field filled in...) then
            // use ChangeSupport to implement add/removeChangeListener below.
            // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public void readSettings(EditRemoteTargetMarkersEdit wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(EditRemoteTargetMarkersEdit wiz) {
            getComponent().storeSelection(wiz);
        }
    }

}
