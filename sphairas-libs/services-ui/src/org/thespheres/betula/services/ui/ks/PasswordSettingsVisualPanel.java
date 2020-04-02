/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.ks;

import com.google.common.io.Resources;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.keyring.Keyring;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.services.ui.KeyStores;

@NbBundle.Messages({"PasswordSettingsVisualPanel.step.name=Passwort"})
final class PasswordSettingsVisualPanel extends JPanel {

    private PasswordSettingsVisualPanel() {
        try {
            URL ksInfo = PrivacyVisualPanel.class.getResource("/org/thespheres/betula/services/ui/ks/keyStoreInfo.html");
            URL kRingInfo = PrivacyVisualPanel.class.getResource("/org/thespheres/betula/services/ui/ks/keyRingInfo.html");
            initComponents();
            keyStoreInfoLabel.setText(Resources.toString(ksInfo, Charset.forName("utf-8")));
            keyRingInfoLabel.setText(Resources.toString(kRingInfo, Charset.forName("utf-8")));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PrivacyVisualPanel.class, "PasswordSettingsVisualPanel.step.name");
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        keyStoreInfoLabel = new org.jdesktop.swingx.JXLabel();
        storeKeyStorePasswordCheckBox = new javax.swing.JCheckBox();
        passwordTextFieldLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();
        keyRingInfoLabel = new org.jdesktop.swingx.JXLabel();

        setPreferredSize(new java.awt.Dimension(300, 300));

        keyStoreInfoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        keyStoreInfoLabel.setLineWrap(true);

        org.openide.awt.Mnemonics.setLocalizedText(storeKeyStorePasswordCheckBox, org.openide.util.NbBundle.getMessage(PasswordSettingsVisualPanel.class, "PasswordSettingsVisualPanel.storeKeyStorePasswordCheckBox.text")); // NOI18N

        passwordTextFieldLabel.setLabelFor(passwordTextField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordTextFieldLabel, org.openide.util.NbBundle.getMessage(PasswordSettingsVisualPanel.class, "PasswordSettingsVisualPanel.passwordTextFieldLabel.text")); // NOI18N

        keyRingInfoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        keyRingInfoLabel.setLineWrap(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keyStoreInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passwordTextFieldLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(storeKeyStorePasswordCheckBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                            .addComponent(keyRingInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(keyStoreInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordTextFieldLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(storeKeyStorePasswordCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keyRingInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private boolean isValidPassword() {
        return passwordTextField.getPassword() != null
                && passwordTextField.getPassword().length != 0;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXLabel keyRingInfoLabel;
    private org.jdesktop.swingx.JXLabel keyStoreInfoLabel;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JLabel passwordTextFieldLabel;
    private javax.swing.JCheckBox storeKeyStorePasswordCheckBox;
    // End of variables declaration//GEN-END:variables

    static class PasswordSettingsPanel implements WizardDescriptor.Panel<WizardDescriptor>, DocumentListener, ActionListener {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private PasswordSettingsVisualPanel component;
        private final ChangeSupport cSupport = new ChangeSupport(this);
        private boolean valid = false;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public PasswordSettingsVisualPanel getComponent() {
            if (component == null) {
                component = new PasswordSettingsVisualPanel();
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
            return valid;
            // If it depends on some condition (form filled out...) and
            // this condition changes (last form field filled in...) then
            // use ChangeSupport to implement add/removeChangeListener below.
            // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
        }

        private void updateValid() {
            boolean old = valid;
            valid = !getComponent().storeKeyStorePasswordCheckBox.isSelected() || getComponent().isValidPassword();
            if (old != valid) {
                cSupport.fireChange();
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cSupport.removeChangeListener(l);
        }

        @Override
        public void readSettings(WizardDescriptor wiz) {
            getComponent().passwordTextField.getDocument().addDocumentListener(this);
            getComponent().storeKeyStorePasswordCheckBox.addActionListener(this);
            updateValid();
        }

        @Override
        public void storeSettings(WizardDescriptor wiz) {
            getComponent().passwordTextField.getDocument().removeDocumentListener(this);
            getComponent().storeKeyStorePasswordCheckBox.removeActionListener(this);
            if (getComponent().isValidPassword()) {
                String description = NbBundle.getMessage(KeyStores.class, "KeyStores.keyStore.password.description", new Date());
                Keyring.save(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY, getComponent().passwordTextField.getPassword(), description);
            } else {
                Keyring.delete(KeyStores.KEYRING_KEYSTORE_PASSWORD_KEY);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateValid();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateValid();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateValid();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateValid();
        }

    }
}
