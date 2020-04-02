/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.project;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.project.BetulaProjectUtil;
import org.thespheres.betula.project.ServiceProjectTemplate;
import org.thespheres.betula.services.implementation.ui.project.SelectPatternVisualPanel.SelectPatternPanel;

public class BasePatternProjectWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {

    public static final String PROJECT_PROPERTIES_FILENAME = "project.properties";
    static final String PROP_PROVIDER_LIST = "provider.list";
    static final String PROP_PROVIDER = "provider";
    static final String PROP_PATTERN = "pattern";
    static final String PROP_PROPERTIES = "properties";
    private int index;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private WizardDescriptor wizard;

    public BasePatternProjectWizardIterator() {
    }

    @Messages({"betula.template.displayName=Jahrgang"})
    @TemplateRegistration(folder = "Project/Kursmappen",
            position = 5000,
            content = "pattern-project-template.properties",
            displayName = "#betula.template.displayName",
            iconBase = "org/thespheres/betula/services/implementation/ui/project/betulaproject16.png",
            description = "projectDescription.html")
    public static BasePatternProjectWizardIterator createIterator() {
        return new BasePatternProjectWizardIterator();
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        final List<PatternProvider> l = Lookup.getDefault().lookupAll(ServiceProjectTemplate.Provider.class).stream()
                .map(p -> p.findTemplates(PatternProvider.class))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        wiz.putProperty(PROP_PROVIDER_LIST, l);
        final FileObject t = Templates.getTemplate(wiz);
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
        panels.add(new SelectPatternPanel());
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
        wizard.putProperty(PROP_PROVIDER, null);
        wizard.putProperty(PROP_PATTERN, null);
        wizard.putProperty(PROP_PROPERTIES, null);
        wizard = null;
        panels = null;
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        final PatternProvider provider = (PatternProvider) wizard.getProperty(PROP_PROVIDER);
        final PatternProvider.PatternSelection selection = (PatternProvider.PatternSelection) wizard.getProperty(PROP_PATTERN);
        final Properties properties = (Properties) wizard.getProperty(PROP_PROPERTIES);
        if (provider == null || selection == null || properties == null) {
            return Collections.EMPTY_SET;
        }
        selection.createProjectProperties(properties);
        final String enc;
        try {
            enc = URLEncoder.encode(provider.getProviderInfo().getURL(), "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex); //Should never happen.
        }
        final String name = enc + "_" + selection.getDisplayName();
        final Path project = ProjectsSupport.projectsBaseFolder().resolve(name);
        BetulaProjectUtil.createProject(project, properties);
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
