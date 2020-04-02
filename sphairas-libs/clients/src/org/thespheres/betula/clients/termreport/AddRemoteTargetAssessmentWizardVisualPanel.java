/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.termreport;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;

@Messages({"AddRemoteTargetAssessmentWizardVisualPanel.step.name=Liste und Halbjahr"})
final class AddRemoteTargetAssessmentWizardVisualPanel extends JPanel implements StringValue {

    private NamingResolver nr;
    private final DefaultComboBoxModel targetBoxModel = new DefaultComboBoxModel();
    private final DefaultComboBoxModel termBoxModel = new DefaultComboBoxModel();
    private final StringValue termStringValue = v -> v instanceof Term ? ((Term) v).getDisplayName() : " ";

    AddRemoteTargetAssessmentWizardVisualPanel() {
        initComponents();
        targetComboBox.setRenderer(new DefaultListRenderer(this));
        termComboBox.setRenderer(new DefaultListRenderer(termStringValue));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(AddRemoteTargetAssessmentWizardVisualPanel.class, "AddRemoteTargetAssessmentWizardVisualPanel.step.name");
    }

    @Override
    public String getString(Object value) {
        if (value instanceof DocumentId) {
            DocumentId d = (DocumentId) value;
            if (nr != null) {
                NamingResolver.Result r;
                try {
                    r = nr.resolveDisplayNameResult(d);
                    Term t = (Term) termBoxModel.getSelectedItem();
                    return r.getResolvedName(t);
                } catch (IllegalAuthorityException ex) {
                    return d.getId();
                }
            }
        }
        return " ";
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetComboBox = new org.jdesktop.swingx.JXComboBox();
        targetLabel = new javax.swing.JLabel();
        termLabel = new javax.swing.JLabel();
        termComboBox = new org.jdesktop.swingx.JXComboBox();

        targetComboBox.setModel(targetBoxModel);

        org.openide.awt.Mnemonics.setLocalizedText(targetLabel, org.openide.util.NbBundle.getMessage(AddRemoteTargetAssessmentWizardVisualPanel.class, "AddRemoteTargetAssessmentWizardVisualPanel.targetLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(termLabel, org.openide.util.NbBundle.getMessage(AddRemoteTargetAssessmentWizardVisualPanel.class, "AddRemoteTargetAssessmentWizardVisualPanel.termLabel.text")); // NOI18N

        termComboBox.setModel(termBoxModel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(termLabel)
                    .addComponent(targetLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(termComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                    .addComponent(targetComboBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetLabel)
                    .addComponent(targetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(termLabel)
                    .addComponent(termComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXComboBox targetComboBox;
    private javax.swing.JLabel targetLabel;
    private org.jdesktop.swingx.JXComboBox termComboBox;
    private javax.swing.JLabel termLabel;
    // End of variables declaration//GEN-END:variables

    static class AddRemoteTargetAssessmentWizardPanel implements WizardDescriptor.Panel<RemoteServiceDescriptor> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private AddRemoteTargetAssessmentWizardVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public AddRemoteTargetAssessmentWizardVisualPanel getComponent() {
            if (component == null) {
                component = new AddRemoteTargetAssessmentWizardVisualPanel();
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
        public void readSettings(RemoteServiceDescriptor wiz) {
            final DefaultComboBoxModel tbm = getComponent().termBoxModel;
            tbm.removeAllElements();
            wiz.findSelectableTerms().forEach(tbm::addElement);
            Term t = wiz.getSelectedTerm();
            if (t != null) {
                tbm.setSelectedItem(t);
            }
            getComponent().nr = wiz.getNamingResolver();
            final DefaultComboBoxModel tgbm = getComponent().targetBoxModel;
            tgbm.removeAllElements();
            wiz.getDocuments().forEach(tgbm::addElement);
            DocumentId d = wiz.getSelectedDocument();
            if (d != null) {
                tgbm.setSelectedItem(d);
            }
        }

        @Override
        public void storeSettings(RemoteServiceDescriptor wiz) {
            final DefaultComboBoxModel tbm = getComponent().termBoxModel;
            wiz.setSelectedTerm((Term) tbm.getSelectedItem());
            final DefaultComboBoxModel tgbm = getComponent().targetBoxModel;
            wiz.setSelectedDocument((DocumentId) tgbm.getSelectedItem());
        }

    }
}
