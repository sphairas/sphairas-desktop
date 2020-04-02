/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.termreport.wizard.CreateTermReportFileVisualPanel.CreateTermReportFilePanel;

@TemplateRegistration(folder = "Betula",
        position = 2000,
        requireProject = true,
        content = "/org/thespheres/betula/termreport/wizard/report_template.xml",
        iconBase = "org/thespheres/betula/termreport/resources/betulatrep2_16.png",
        displayName = "#TermReportWizardIterator.displayName",
        targetName = "#TermReportWizardIterator.targetName",
        description = "/org/thespheres/betula/termreport/wizard/report.html")
@Messages({"TermReportWizardIterator.displayName=Zensurenbogen",
    "TermReportWizardIterator.targetName=Halbjahr"})
public final class TermReportWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    static final String PROP_LOCAL_FILE_PROPERTIES = "local.file.properties";
    private int index;
    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    /**
     * Initialize panels representing individual wizard's steps and sets various
     * properties for them influencing wizard appearance.
     */
    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            Project p = Templates.getProject(wizard);
            WizardDescriptor.Panel<WizardDescriptor> create
                    = Templates.buildSimpleTargetChooser(p, ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)).bottomPanel(new CreateTermReportFilePanel()).create();
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
    public Set instantiate() throws IOException {
        DataObject template = DataObject.find(Templates.getTemplate(wizard));
        FileObject folder = Templates.getTargetFolder(wizard);
        String targetName = Templates.getTargetName(wizard);
//        String freeFile = FileUtil.findFreeFileName(folder, targetName, "xml");
        DataFolder df = DataFolder.findFolder(folder);
//        DataObject newFile = template.createFromTemplate(df, freeFile);
        DataObject dob = template.createFromTemplate(df, targetName);
        return Collections.singleton(dob);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        LocalFileProperties props = Templates.getProject(wizard).getLookup().lookup(LocalFileProperties.class);
        wizard.putProperty(PROP_LOCAL_FILE_PROPERTIES, props);
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
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

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
}
