/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.updates;

import java.io.File;
import javax.swing.JPanel;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author boris.heithecker
 */
class FolderLocationPanel extends JPanel {

    private final FileWatcherOptions options;

    FolderLocationPanel(FileWatcherOptions options) {
        this.options = options;
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        enableWatchCheckBox = new javax.swing.JCheckBox();
        folderLabel = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        BrowseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(enableWatchCheckBox, org.openide.util.NbBundle.getMessage(FolderLocationPanel.class, "FolderLocationPanel.enableWatchCheckBox.text")); // NOI18N
        enableWatchCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableWatchCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(folderLabel, org.openide.util.NbBundle.getMessage(FolderLocationPanel.class, "FolderLocationPanel.folderLabel.text")); // NOI18N

        folderTextField.setEditable(false);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, enableWatchCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), folderTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        org.openide.awt.Mnemonics.setLocalizedText(BrowseButton, org.openide.util.NbBundle.getMessage(FolderLocationPanel.class, "FolderLocationPanel.BrowseButton.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, enableWatchCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), BrowseButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        BrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(folderLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(folderTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(enableWatchCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableWatchCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(folderLabel)
                    .addComponent(folderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BrowseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void enableWatchCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableWatchCheckBoxActionPerformed
        options.changed();
    }//GEN-LAST:event_enableWatchCheckBoxActionPerformed

    @NbBundle.Messages({"FolderLocationPanel.FileChooser.Title=Order für die Untis-Vertretungen Datei"})
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        File home = new File(System.getProperty("user.home"));
        String title = NbBundle.getMessage(FolderLocationPanel.class, "FolderLocationPanel.FileChooser.Title");
        FileChooserBuilder fcb = new FileChooserBuilder(FolderLocationPanel.class);
        fcb.setTitle(title)
                .setDefaultWorkingDirectory(home).setFileHiding(true).setDirectoriesOnly(true); //.setFileFilter(FILE_FILTER);
        File open = fcb.showOpenDialog();
        if (open != null && open.exists()) {
            folderTextField.setText(open.getAbsolutePath());
            options.changed();
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    void store() {
        final boolean watch = enableWatchCheckBox.isSelected();
        options.setWatchFoler(watch);
        FileObject folder = getValidFolder();
        if (folder != null) {
            options.setWatchedFolder(folder);
        }
    }

    private FileObject getValidFolder() {
        final String path = StringUtils.trimToNull(folderTextField.getText());
        if (path != null) {
            final FileObject folder = FileUtil.toFileObject(new File(path));
            if (folder != null && folder.isFolder()) {
                return folder;
            }
        }
        return null;
    }

    void load() {
        enableWatchCheckBox.setSelected(options.isWatchFolder());
        final FileObject folder = options.getWatchedFolder();
        if (folder != null) {
            folderTextField.setText(folder.getPath());
        }
    }

    boolean valid() {
        final boolean enabled = enableWatchCheckBox.isSelected();
        return enabled ? getValidFolder() != null : true;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BrowseButton;
    private javax.swing.JCheckBox enableWatchCheckBox;
    private javax.swing.JLabel folderLabel;
    private javax.swing.JTextField folderTextField;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
