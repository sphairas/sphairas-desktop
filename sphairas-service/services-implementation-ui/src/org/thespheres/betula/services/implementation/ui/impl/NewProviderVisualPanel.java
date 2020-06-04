/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import com.google.common.io.Resources;
import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.BorderUIResource;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXLabel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 *
 * @author boris.heithecker
 */
class NewProviderVisualPanel extends javax.swing.JPanel {

    static final int WAIT_TIME = 2700;
    private final HostnameListener listener = new HostnameListener();
    private final RequestProcessor rp = new RequestProcessor();
    private final RequestProcessor.Task updateUrl;

    @SuppressWarnings(value = {"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    public NewProviderVisualPanel() {
        initComponents();
        updateUrl = rp.create(this::setUrlFromHostname);
        final URL agree = NewProviderVisualPanel.class.getResource("/org/thespheres/betula/services/implementation/ui/resources/newProviderKeyInfo.html");
        initComponents();
        ((JXLabel) keyInfoLabel).setLineWrap(true);
        updateAliasTextField(null);
        try {
            keyInfoLabel.setText(Resources.toString(agree, Charset.forName("utf-8")));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    void setUrlFromHostname() {
        final String host = StringUtils.trimToNull(hostTextField.getText());
        if (host == null) {
            //TODO: user Apache Commons Validation DomainValidator to validate
            final boolean valid = true;
            if (!valid) {
                final Border redBorder = new BorderUIResource(BorderFactory.createLineBorder(Color.RED, 2));
                hostTextField.setBorder(redBorder);
            }
        }
//        SwingUtilities.updateComponentTreeUI(hostTextField);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chooseButtonGroup = new javax.swing.ButtonGroup();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        updateAliasTextField = new javax.swing.JCheckBox();
        aliasLabel = new javax.swing.JLabel();
        aliasTextField = new javax.swing.JTextField();
        keyInfoLabel = new JXLabel();

        setPreferredSize(new java.awt.Dimension(300, 300));

        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(NewProviderVisualPanel.class, "NewProviderVisualPanel.hostLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(updateAliasTextField, org.openide.util.NbBundle.getMessage(NewProviderVisualPanel.class, "NewProviderVisualPanel.updateAliasTextField.text")); // NOI18N
        updateAliasTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAliasTextField(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(aliasLabel, org.openide.util.NbBundle.getMessage(NewProviderVisualPanel.class, "NewProviderVisualPanel.aliasLabel.text")); // NOI18N

        keyInfoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(updateAliasTextField)
                .addGap(0, 117, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hostTextField))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keyInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(aliasLabel)
                                .addGap(26, 26, 26)
                                .addComponent(aliasTextField)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(updateAliasTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aliasLabel)
                    .addComponent(aliasTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(keyInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateAliasTextField(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAliasTextField
        final boolean enable = updateAliasTextField.isSelected();
        aliasTextField.setEnabled(enable);
    }//GEN-LAST:event_updateAliasTextField


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aliasLabel;
    private javax.swing.JTextField aliasTextField;
    private javax.swing.ButtonGroup chooseButtonGroup;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JLabel keyInfoLabel;
    private javax.swing.JCheckBox updateAliasTextField;
    // End of variables declaration//GEN-END:variables

    private class HostnameListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateUrl.schedule(WAIT_TIME);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateUrl.schedule(WAIT_TIME);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateUrl.schedule(WAIT_TIME);
        }

    }

    static class NewProviderPanel implements WizardDescriptor.Panel<WizardDescriptor> {

        private NewProviderVisualPanel component;

        @Override
        public NewProviderVisualPanel getComponent() {
            if (component == null) {
                component = new NewProviderVisualPanel();
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public void readSettings(WizardDescriptor w) {
            final NewProviderVisualPanel c = getComponent();
            final String host = (String) w.getProperty(NewProviderAction.PROP_HOST);
            if (host != null) {
                c.hostTextField.setText(host);
            }
            final String alias = (String) w.getProperty(NewProviderAction.PROP_ALIAS);
            if (alias != null) {
                c.aliasTextField.setText(alias);
            }
            c.hostTextField.getDocument().addDocumentListener(c.listener);
        }

        @Override
        public void storeSettings(WizardDescriptor w) {
            final NewProviderVisualPanel c = getComponent();
            c.hostTextField.getDocument().removeDocumentListener(c.listener);
            final String host = StringUtils.trimToNull(c.hostTextField.getText());
            w.putProperty(NewProviderAction.PROP_HOST, host);
            if (c.aliasTextField.isEnabled()) {
                final String alias = StringUtils.trimToNull(c.aliasTextField.getText());
                w.putProperty(NewProviderAction.PROP_ALIAS, alias);
            }
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    }

}
