/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv.impl;

import java.io.File;
import java.nio.file.Path;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.thespheres.betula.admindocsrv.DownloadTargetFolders;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SelectDownloadFolderPanel.selectButtonActionPerformed.title=Download-Ordner wählen"})
class SelectDownloadFolderPanel extends javax.swing.JPanel {

    private final DownloadOptionsPanelController controller;
    private Path selected;

    SelectDownloadFolderPanel(DownloadOptionsPanelController parent) {
        this.controller = parent;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dirLabel = new javax.swing.JLabel();
        dirTextField = new javax.swing.JTextField();
        selectButton = new javax.swing.JButton();
        sevenZCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(dirLabel, org.openide.util.NbBundle.getMessage(SelectDownloadFolderPanel.class, "SelectDownloadFolderPanel.dirLabel.text")); // NOI18N

        dirTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(selectButton, org.openide.util.NbBundle.getMessage(SelectDownloadFolderPanel.class, "SelectDownloadFolderPanel.selectButton.text")); // NOI18N
        selectButton.setActionCommand(org.openide.util.NbBundle.getMessage(SelectDownloadFolderPanel.class, "SelectDownloadFolderPanel.selectButton.actionCommand")); // NOI18N
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sevenZCheckBox, org.openide.util.NbBundle.getMessage(SelectDownloadFolderPanel.class, "SelectDownloadFolderPanel.sevenZCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(dirTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectButton))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dirLabel)
                    .addComponent(sevenZCheckBox))
                .addGap(0, 185, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(dirLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(sevenZCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        final String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) {
            final File home = new File(System.getProperty("user.home"));
            final String title = NbBundle.getMessage(SelectDownloadFolderPanel.class, "SelectDownloadFolderPanel.selectButtonActionPerformed.title");
            final FileChooserBuilder fcb = new FileChooserBuilder(SelectDownloadFolderPanel.class);
            fcb.setTitle(title).setDefaultWorkingDirectory(home).setDirectoriesOnly(true).setFileHiding(true);
            final File folder = fcb.showOpenDialog();
            if (folder != null) {
                selected = folder.toPath();
                dirTextField.setText(selected.toString());
            } else {
                load();
            }
        }
    }//GEN-LAST:event_selectButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dirLabel;
    private javax.swing.JTextField dirTextField;
    private javax.swing.JButton selectButton;
    private javax.swing.JCheckBox sevenZCheckBox;
    // End of variables declaration//GEN-END:variables

    void load() {
        // TODO read settings and initialize GUI
        // Example:        
        final String pref = NbPreferences.forModule(DownloadTargetFolders.class).get(DownloadTargetFolders.PREF_DOWNLOAD_TARGET_FOLER, null);
        dirTextField.setText(pref);
        final boolean seven7 = NbPreferences.forModule(DownloadTargetFolders.class).getBoolean(DownloadTargetFolders.PREF_USE_7Z, false);
        sevenZCheckBox.setSelected(seven7);
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(RemoteUnitsPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
    }

    void store() {
        // TODO store modified settings
        // Example:
        if (selected != null) {
            NbPreferences.forModule(DownloadTargetFolders.class).put(DownloadTargetFolders.PREF_DOWNLOAD_TARGET_FOLER, selected.toString());
        }
        NbPreferences.forModule(DownloadTargetFolders.class).putBoolean(DownloadTargetFolders.PREF_USE_7Z, sevenZCheckBox.isSelected());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
