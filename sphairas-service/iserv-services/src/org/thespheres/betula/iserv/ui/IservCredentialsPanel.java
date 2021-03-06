package org.thespheres.betula.iserv.ui;

import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import org.apache.commons.lang3.StringUtils;

final class IservCredentialsPanel extends JPanel {

    private final IservCredentialsController controller;
    private final IservUsernameFormatter usernameFormatter = new IservUsernameFormatter();
    private final PasswortTextFieldListener pwListener = new PasswortTextFieldListener();
    private boolean passwordUserEdit;

    IservCredentialsPanel(IservCredentialsController controller) {
        this.controller = controller;
        initComponents();
        this.passwordField.getDocument().addDocumentListener(pwListener);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        userNameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        usernameTextField = new JFormattedTextField(usernameFormatter);
        privacyLabel = new org.jdesktop.swingx.JXLabel();

        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(IservCredentialsPanel.class, "IservCredentialsPanel.userNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(IservCredentialsPanel.class, "IservCredentialsPanel.passwordLabel.text")); // NOI18N

        privacyLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        privacyLabel.setLineWrap(true);
        privacyLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(119, 119, 119)
                        .addComponent(privacyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userNameLabel)
                            .addComponent(passwordLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(usernameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                            .addComponent(passwordField))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(privacyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        usernameTextField.setText(controller.loadIservUser());
        passwordField.setText(controller.hasStoredPassword() ? "password" : null);
        passwordField.selectAll();
        passwordUserEdit = false;
        privacyLabel.setText(controller.getHtmlPrivacyMessage());
    }

    void store() {
        controller.storeIservUser(StringUtils.trimToNull(usernameTextField.getText()));
        if (passwordUserEdit) {
            controller.storeIservPassword(passwordField.getPassword());
        }
    }

    boolean valid() {
        final boolean passwordValid = (controller.hasStoredPassword() && !passwordUserEdit) || passwordField.getPassword().length != 0;
        return usernameTextField.isEditValid() && passwordValid;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private org.jdesktop.swingx.JXLabel privacyLabel;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JFormattedTextField usernameTextField;
    // End of variables declaration//GEN-END:variables

    private class IservUsernameFormatter extends DefaultFormatter implements DocumentListener {

        private JFormattedTextField jft;
        private final Pattern pattern = Pattern.compile("([\\w]+(-[\\w]+)*)");

        @Override
        public void install(JFormattedTextField ftf) {
            super.install(ftf);
            this.jft = ftf;
            this.jft.getDocument().addDocumentListener(this);
        }

        @Override
        public void uninstall() {
            super.uninstall();
            if (this.jft != null) {
                this.jft.getDocument().removeDocumentListener(this);
                this.jft = null;
            }
        }

        private void check() {
            String t = jft.getText();
            if (!pattern.matcher(t).matches()) {
                invalidEdit();
            }
            controller.changed();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            check();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            check();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            check();
        }

    }

    private class PasswortTextFieldListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            passwordUserEdit = true;
            controller.changed();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            passwordUserEdit = true;
            controller.changed();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            passwordUserEdit = true;
            controller.changed();
        }

    }
}
