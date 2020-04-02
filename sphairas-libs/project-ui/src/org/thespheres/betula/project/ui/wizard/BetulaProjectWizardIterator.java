/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.project.BetulaProjectType;
import org.thespheres.betula.project.BetulaProjectUtil;
import org.thespheres.betula.project.UnitTargetProjectTemplate.UnitTargetSelection;
import org.thespheres.betula.project.ui.wizard.SelectProjectLocationVisualPanel.SelectProjectLocationPanel;
import org.thespheres.betula.project.ui.wizard.SelectUnitTargetVisualPanel.SelectUnitTargetPanel;

public class BetulaProjectWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    static final String PROP_PROPERTIES = "betula.project.wizard.properties";
    static final String PROP_TEMPLATE = "betula.project.wizard.template";
//    static final String PROP_CONFIGURATOR = "betula.project.wizard.configurator";
    static final String PROP_PROJECT_DIR = "betula.project.wizard.projectDir";
    static final String PROP_TYPE = "betula.project.wizard.type";
    static final String PROP_USE_EXISTING_FOLDER = "betula.project.wizard.use.existing.folder";
    static final String PROP_PROJECT_PATH = "betula.project.wizard.project.path";
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private WizardDescriptor wizard;

    @Messages({"betula.template.displayName=Kursmappe"})
    @TemplateRegistration(folder = "Project/Kursmappen",
            position = 1000,
            content = "template.properties",
            displayName = "#betula.template.displayName",
            iconBase = "org/thespheres/betula/project/ui/resources/betulaproject16.png",
            requireProject = false,
            description = "projectDescription.html")
    public static BetulaProjectWizardIterator createIterator() {
        return new BetulaProjectWizardIterator();
    }

    @Override
    public void initialize(WizardDescriptor wiz) { 
        FileObject t = Templates.getTemplate(wiz);
        try (final InputStream is = t.getInputStream()) {
            final Properties p = new Properties();
            p.load(is);
            wiz.putProperty(PROP_PROPERTIES, p);
        } catch (IOException ioex) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ioex);
        }
        wizard = wiz;
        index = 0;
        panels = new ArrayList<>();
        panels.add(new SelectUnitTargetPanel());
        panels.add(new SelectProjectLocationPanel());
        // Make sure list of steps is accurate.
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        wizard.putProperty(PROP_PROPERTIES, null);
        wizard.putProperty(PROP_TEMPLATE, null);
        wizard.putProperty(PROP_PROJECT_DIR, null);
        wizard = null;
        panels = null;
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        final BetulaProjectType type = (BetulaProjectType) wizard.getProperty(PROP_TYPE);
        final Path project = (Path) wizard.getProperty(PROP_PROJECT_PATH);

        Properties prop = null;
        if (type.equals(BetulaProjectType.PROVIDER)) {
            final UnitTargetSelection data = (UnitTargetSelection) wizard.getProperty(PROP_TEMPLATE);
            prop = data.createProjectProperties(null);
        } else if (type.equals(BetulaProjectType.LOCAL)) {
            prop = new Properties();
            String baseName = project.getName(project.getNameCount() - 1).toString();
            baseName = StringUtils.substringBefore(baseName, ".");
            baseName = StringUtils.trimToEmpty(baseName);
            baseName = baseName.replaceAll("\\W+", "-");
            prop.setProperty("unit.id", baseName);
        }
        if (prop == null) {
            throw new IOException();
        }

        BetulaProjectUtil.createProject(project, prop, type);

        return Collections.singleton(FileUtil.toFileObject(project.toFile()));
    }

    @Override
    public String name() {
        return MessageFormat.format("{0}. von {1}", new Object[]{index + 1, panels.size()});
    }

    @Override
    public boolean hasNext() {
        return index < panels.size() - 1;
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

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
}
