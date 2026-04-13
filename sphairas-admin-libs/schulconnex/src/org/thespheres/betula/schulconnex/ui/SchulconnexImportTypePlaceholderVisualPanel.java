/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.thespheres.betula.schulconnex.SchulconnexImportData;

/**
 * Placeholder panel for type-specific selection/review steps.
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({
    "SchulconnexImportTypePlaceholderVisualPanel.signee.step.name=Lehrende auswaehlen",
    "SchulconnexImportTypePlaceholderVisualPanel.primaryUnit.step.name=Klassen und Schueler auswaehlen",
    "SchulconnexImportTypePlaceholderVisualPanel.targetItem.step.name=Kurse auswaehlen",
    "SchulconnexImportTypePlaceholderVisualPanel.todo.message=Schulconnex-API-Anbindung folgt in einem naechsten Schritt."
})
final class SchulconnexImportTypePlaceholderVisualPanel extends JPanel {

    private final String type;

    SchulconnexImportTypePlaceholderVisualPanel(final String type) {
        this.type = type;
        initComponents();
    }

    @Override
    public String getName() {
        switch (type) {
            case SchulconnexImportAction.PRIMARY_UNIT:
                return NbBundle.getMessage(SchulconnexImportTypePlaceholderVisualPanel.class,
                        "SchulconnexImportTypePlaceholderVisualPanel.primaryUnit.step.name");
            case SchulconnexImportAction.TARGET_ITEM:
                return NbBundle.getMessage(SchulconnexImportTypePlaceholderVisualPanel.class,
                        "SchulconnexImportTypePlaceholderVisualPanel.targetItem.step.name");
            case SchulconnexImportAction.SIGNEE:
            default:
                return NbBundle.getMessage(SchulconnexImportTypePlaceholderVisualPanel.class,
                        "SchulconnexImportTypePlaceholderVisualPanel.signee.step.name");
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        final JLabel text = new JLabel(
                NbBundle.getMessage(SchulconnexImportTypePlaceholderVisualPanel.class,
                        "SchulconnexImportTypePlaceholderVisualPanel.todo.message"),
                SwingConstants.CENTER);
        add(text, BorderLayout.CENTER);
    }

    static final class SchulconnexImportTypePlaceholderPanel implements WizardDescriptor.Panel<SchulconnexImportData> {

        private final String type;
        private SchulconnexImportTypePlaceholderVisualPanel component;

        SchulconnexImportTypePlaceholderPanel(final String type) {
            this.type = type;
        }

        @Override
        public SchulconnexImportTypePlaceholderVisualPanel getComponent() {
            if (component == null) {
                component = new SchulconnexImportTypePlaceholderVisualPanel(type);
            }
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
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

        @Override
        public void readSettings(SchulconnexImportData wiz) {
        }

        @Override
        public void storeSettings(SchulconnexImportData wiz) {
        }
    }
}
