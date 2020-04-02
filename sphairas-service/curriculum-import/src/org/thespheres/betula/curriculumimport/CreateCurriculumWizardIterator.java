/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport;

import com.google.common.net.UrlEscapers;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.actions.Savable;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.adminconfig.ConfigurationBuilder;
import org.thespheres.betula.curriculumimport.CreateCurriculumWizardTargetVisualPanel.CreateCurriculumWizardTargetPanel;
import org.thespheres.betula.curriculumimport.config.CurriculumConfigNodeList;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.ui.FileInfo;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

public final class CreateCurriculumWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    public static final String PROP_NAME = "name";
    private int index;
    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    static final RequestProcessor RP = new RequestProcessor();
    static final DateTimeFormatter FILE_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            final WizardDescriptor.Panel<WizardDescriptor> create = new CreateCurriculumWizardTargetPanel();
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
        final ConfigurableImportTarget p = (ConfigurableImportTarget) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        final String fileDisplayName = (String) wizard.getProperty(CreateCurriculumWizardIterator.PROP_NAME);
        final String provider = p.getProvider();

        final File tmpDir = Files.createTempDirectory(null).toFile();
        final DataFolder tmpFolder = DataFolder.findFolder(FileUtil.toFileObject(tmpDir));
        final DataObject d = template.createFromTemplate(tmpFolder);
        final FileInfo fi = d.getLookup().lookup(FileInfo.class);
        fi.setFileDisplayName(fileDisplayName);
        d.setModified(true);
        d.getLookup().lookup(Savable.class).save();
        final byte[] file = IOUtils.toByteArray(d.getPrimaryFile().getInputStream());
        tmpDir.delete();

        final String baseName = StringUtils.removePattern(fileDisplayName, "[^\\p{L}\\p{N}]");
        final String dateExtension = FILE_DATETIME.format(LocalDateTime.now());
        final String relativeName = "signee/" + baseName + dateExtension + ".xml";

        final ConfigurationBuilder builder = ConfigurationBuilder.find(provider);

        if (builder != null) {
            final String resource = CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME + "," + relativeName;
            builder.buildResources(resource, cbt -> {
                try {
                    final Path res = cbt.getProviderBasePath().resolve(CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME);
                    final List<String> list;
                    if (Files.exists(res)) {
                        list = Files.readAllLines(res, StandardCharsets.UTF_8);
                        if (list.contains(relativeName)) {
                            throw new IOException("Resource already contained in curriculum-files");
                        }
                    } else {
                        list = new ArrayList<>();
                        list.add("#Synchronization of curriculum files");
                    }
                    list.add("#Added: " + LocalDateTime.now().toString());
                    list.add(relativeName);
                    final String encoded = UrlEscapers.urlFragmentEscaper().escape(relativeName);
                    HttpUtilities.putIfNoneMatch(cbt.getWebProvider(), cbt.resolveResource(encoded), file, null, cbt.getLockToken());
                    HttpUtilities.put(cbt.getWebProvider(), cbt.resolveResource(CurriculumConfigNodeList.PROVIDER_FILE_LIST_NAME), cbt.getBytes(list), null, cbt.getLockToken());
                } catch (IOException ex) {
                    cbt.cancel(ex);
                }

            });
        }
        return Collections.EMPTY_SET;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
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
