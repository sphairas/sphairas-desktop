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
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.*;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.thespheres.betula.services.LocalFileProperties;

// TODO define position attribute
@TemplateRegistration(folder = "Betula",
        requireProject = false,
        content = "ZeugnisDataFileTemplate.xml",
        displayName = "#ZeugnisDataFileTemplate.displayName",
        targetName = "#ZeugnisDataFileTemplate.target")
@Messages({"CreateJournalFileWizardIterator.displayName=Berichtsheft",
    "CreateJournalFileWizardIterator.targetName=Quartal"})
public final class CreateJournalFileWizardIterator1 implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    public static final String PRESELECTED_TARGET_FOLDER = "CreateJournalFileWizardIterator.preselectedTargetFolder";
    static final String PROP_LOCAL_FILE_PROPERTIES = "local.file.properties";
    static final String PROP_PREFERRED_CONVENTION = "preferredConvention";
    static final String PROP_BEGIN_DATE = "begin.date";
    static final String PROP_END_DATE = "end.date";
    private int index;
    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            Project p = Templates.getProject(wizard);
            String target = findProjectFolder(p);
            if (target != null) {
                if (!target.endsWith("/")) {
                    target = target + "/";
                }
                final FileObject targetFolder = p.getProjectDirectory().getFileObject(target);
                //Override only if targetFolder really exisits
                if (targetFolder != null) {
                    Templates.setTargetFolder(wizard, targetFolder);
                }
            }
//            WizardDescriptor.Panel<WizardDescriptor> create = Templates.buildSimpleTargetChooser(p, ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)).bottomPanel(new CreateJournalFilePanel()).create();
//            panels.add(create);
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
        DataObject template = DataObject.find(Templates.getTemplate(wizard));
        FileObject folder = Templates.getTargetFolder(wizard);
        String targetName = Templates.getTargetName(wizard);
        DataFolder df = DataFolder.findFolder(folder);
        DataObject create = template.createFromTemplate(df, targetName);
        
        
        Project project = Templates.getProject(wizard);
        assert project != null;//Project required;
        final Date b = (Date) wizard.getProperty(PROP_BEGIN_DATE);
        final Date e = (Date) wizard.getProperty(PROP_END_DATE);
        final String convention = (String) wizard.getProperty(PROP_PREFERRED_CONVENTION);

//        final EditableJournal editor = NbUtilities.waitForLookup(create.getLookup(), EditableJournal.class, 0);
//        if (b != null) {
//            editor.setJournalStart(b.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//        }
//        if (e != null) {
//            editor.setJournalEnd(e.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//        }
//        if (convention != null) {
//            XmlTargetAssessment ta = NbUtilities.waitForLookup(create.getLookup(), XmlTargetAssessment.class, 0);
//            ta.setPreferredConvention(convention);
//        }
//        new UpdateCalendar2((JournalDataObject) create).actionPerformed(null);
//        if (create.isModified()) {
//            Savable save = create.getLookup().lookup(Savable.class);
//            if (save != null) {
//                save.save();
//            }
//        }
        String f = FileUtil.getRelativePath(project.getProjectDirectory(), folder);
        saveTargetFolder(project, f);
        return Collections.singleton(create);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        LocalFileProperties props = Templates.getProject(wizard).getLookup().lookup(LocalFileProperties.class);
        wizard.putProperty(PROP_LOCAL_FILE_PROPERTIES, props);
        //TODO: set TargetName from HJ
        //Templates.setTargetName(wizard, targetName);
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

    @Messages("CreateJournalFileWizardIterator.preselectedTargetFolder=Berichte")
    private String findProjectFolder(Project p) {
        final LocalFileProperties lfp = p.getLookup().lookup(LocalFileProperties.class);
        String folder;
        if (lfp != null) {
            if ((folder = lfp.getProperty(PRESELECTED_TARGET_FOLDER)) != null) {
                return folder;
            }
        }
        if ((folder = NbPreferences.forModule(CreateJournalFileWizardIterator1.class).get(PRESELECTED_TARGET_FOLDER, null)) != null) {
            return folder;
        }
        return NbBundle.getMessage(CreateJournalFileWizardIterator1.class, PRESELECTED_TARGET_FOLDER);
    }

    private void saveTargetFolder(Project p, String f) {
        final LocalFileProperties lfp = p.getLookup().lookup(LocalFileProperties.class);
        if (lfp != null && lfp.getProperty(PRESELECTED_TARGET_FOLDER) != null) {
            return;
        }
        if (NbBundle.getMessage(CreateJournalFileWizardIterator1.class, PRESELECTED_TARGET_FOLDER).equals(f)) {
            return;
        }
        NbPreferences.forModule(CreateJournalFileWizardIterator1.class).put(PRESELECTED_TARGET_FOLDER, f);
    }

}
