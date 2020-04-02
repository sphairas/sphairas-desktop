/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import org.thespheres.betula.xmlimport.uiutil.CreateDocumentsComponent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXTable;
import org.openide.WizardDescriptor;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"CreateDocumentsVisualPanel.step.name=Dokumente anlegen"})
class UntisCreateDocumentsVisualPanel extends JPanel implements CreateDocumentsComponent {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private UntisCreateDocumentsTableModel model = null;
    private UntisImportData wizard;
    private static ToolbarPool toolbars;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    UntisCreateDocumentsVisualPanel() {
        super();
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        Arrays.stream(toolbars().getToolbars()).forEach(toolbarPanel::add);
        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPanel, BorderLayout.CENTER);
    }

    private synchronized ToolbarPool toolbars() {
        if (toolbars == null) {
            FileObject root = FileUtil.getConfigRoot();
            FileObject fo = null;
            try {
                fo = FileUtil.createFolder(root, "UntisCreateDocumentsVisualPanel/Toolbars");
            } catch (IOException ex) {
                Logger.getLogger(UntisCreateDocumentsVisualPanel.class.getName()).log(Level.CONFIG, "Cannot create UntisCreateDocumentsVisualPanel/Toolbars folder.", ex);
            }
            if (fo == null) {
                throw new IllegalStateException("No UntisCreateDocumentsVisualPanel/Toolbars/");
            }
            DataFolder folder = DataFolder.findFolder(fo);
            toolbars = new ToolbarPool(folder);
        }
        return toolbars;
    }

    void initialize(UntisImportData wiz) {
        if (wizard == null) {
            wizard = wiz;
            UntisImportConfiguration config = (UntisImportConfiguration) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
            model = config.createUntisCreateDocumentsTableModel();
            table.setColumnFactory(model.createColumnFactory(wiz));
            table.setModel(model);
            table.requestFocus();
        }
        model.initialize(wizard);
    }

    @Override
    public JTable getTable() {
        return table;
    }

    @Override
    public UntisImportData getSettings() {
        return wizard;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(UntisCreateDocumentsVisualPanel.class, "CreateDocumentsVisualPanel.step.name");
    }

    public static class UntisCreateDocumentsPanel implements WizardDescriptor.Panel<UntisImportData> {

        private UntisCreateDocumentsVisualPanel component;

        @Override
        public UntisCreateDocumentsVisualPanel getComponent() {
            if (component == null) {
                component = new UntisCreateDocumentsVisualPanel();
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
        public void readSettings(UntisImportData wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(UntisImportData wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }


}
