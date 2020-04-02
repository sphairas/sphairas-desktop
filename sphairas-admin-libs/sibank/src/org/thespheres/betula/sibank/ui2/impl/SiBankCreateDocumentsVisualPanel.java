/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2.impl;

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
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.sibank.ui2.SiBankCreateDocumentsTableModel;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"SiBankCreateDocumentsVisualPanel.step.name=Dokumente anlegen"})
class SiBankCreateDocumentsVisualPanel extends JPanel implements CreateDocumentsComponent<SiBankKursItem, SiBankImportData> {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    private final JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private SiBankCreateDocumentsTableModel model = null;
    private SiBankImportData wizard;
    private static ToolbarPool toolbars;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    SiBankCreateDocumentsVisualPanel() {
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
                fo = FileUtil.createFolder(root, "SiBankCreateDocumentsVisualPanel/Toolbars");
            } catch (IOException ex) {
                Logger.getLogger(SiBankCreateDocumentsVisualPanel.class.getName()).log(Level.CONFIG, "Cannot create SiBankCreateDocumentsVisualPanel/Toolbars folder.", ex);
            }
            if (fo == null) {
                throw new IllegalStateException("No SiBankCreateDocumentsVisualPanel/Toolbars/");
            }
            DataFolder folder = DataFolder.findFolder(fo);
            toolbars = new ToolbarPool(folder);
        }
        return toolbars;
    }

    void initialize(SiBankImportData wiz) {
        if (wizard == null) {
            wizard = wiz;
            SiBankImportTarget config = (SiBankImportTarget) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
            model = config.createCreateDocumentsTableModel2();
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
    public SiBankImportData getSettings() {
        return wizard;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SiBankCreateDocumentsVisualPanel.class, "SiBankCreateDocumentsVisualPanel.step.name");
    }

    public static class SiBankCreateDocumentsPanel implements WizardDescriptor.Panel<SiBankImportData> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private SiBankCreateDocumentsVisualPanel component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public SiBankCreateDocumentsVisualPanel getComponent() {
            if (component == null) {
                component = new SiBankCreateDocumentsVisualPanel();
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
        public void readSettings(SiBankImportData wiz) {
            getComponent().initialize(wiz);
        }

        @Override
        public void storeSettings(SiBankImportData wiz) {
            // use wiz.putProperty to remember current panel state
        }
    }

}
