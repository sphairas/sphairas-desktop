/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.project.*;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file.ProviderChooserVisualPanel.ProviderChooserPanel;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

// TODO define position attribute
@TemplateRegistration(folder = "Betula",
        requireProject = false,
        content = "ZeugnisDataFileTemplate.xml",
        displayName = "#ZeugnisDataFileTemplate.displayName",
        targetName = "#ZeugnisDataFileTemplate.target")
public final class CreateZeugnisDataFileWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            final WizardDescriptor.Panel<WizardDescriptor> create = Templates.buildSimpleTargetChooser(null, new SourceGroup[0]).bottomPanel(new ProviderChooserPanel()).create();
            panels.add(create);
            String[] steps = createSteps();
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    @Override
    public Set<?> instantiate() throws IOException {
        final DataObject template = DataObject.find(Templates.getTemplate(wizard));
        final FileObject folder = Templates.getTargetFolder(wizard);
        final String targetName = Templates.getTargetName(wizard);
        final DataFolder df = DataFolder.findFolder(folder);
        final DataObject create = template.createFromTemplate(df, targetName);

        final ConfigurableImportTarget p = (ConfigurableImportTarget) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final NdsZeugnisFormular report = NbUtilities.waitForLookup(create.getLookup(), NdsZeugnisFormular.class, 0);
        report.setProvider(p.getProvider());

        create.setModified(true);
        create.getLookup().lookup(Savable.class).save();

        return Collections.singleton(create);
    }

    @Override
    public void initialize(final WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(final WizardDescriptor wizard) {
        panels = null;
        this.wizard = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = (String[]) wizard.getProperty("WizardPanel_contentData");
        assert beforeSteps != null : "This wizard may only be used embedded in the template wizard";
        String[] res = new String[(beforeSteps.length - 1) + panels.size()];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels.get(i - beforeSteps.length + 1).getComponent().getName();
            }
        }
        return res;
    }

}
