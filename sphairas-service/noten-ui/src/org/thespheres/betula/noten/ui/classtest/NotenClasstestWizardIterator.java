/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.ui.classtest;

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
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.noten.impl.NotenAssessmentContextFactory;
import org.thespheres.betula.noten.impl.NotenOSAssessmentContextFactory;
import org.thespheres.betula.noten.ui.ClasstestSupport;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.util.Int2;

// TODO define position attribute
@TemplateRegistration(folder = "Betula",
        position = 8000,
        requireProject = true,
        content = "../resources/classtest_template.xml",
        displayName = "#NotenClasstestWizardIterator.template.displayName",
        iconBase = "org/thespheres/betula/noten/ui/resources/betulact16.png",
        targetName = "#NotenClasstestWizardIterator.template.targetName",
        description = "../resources/notenWizard.html")
@Messages({"NotenClasstestWizardIterator.template.displayName=Klassenarbeit",
    "NotenClasstestWizardIterator.template.targetName=Klassenarbeit"})
public final class NotenClasstestWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    public static final String PRESELECTED_TARGET_FOLDER = "NotenClasstestWizardIterator.preselectedTargetFolder";
    static final String PROP_ASSESSMENTCONTEXTFACTORY = "assessment.context.factory";
    private int index;
    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    final static AssessmentContext.Factory<StudentId, Int2>[] CONTEXTS = new AssessmentContext.Factory[]{new NotenAssessmentContextFactory(), new NotenOSAssessmentContextFactory()};

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            Project p = Templates.getProject(wizard);
            String target = findTargetFolder(p);
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
            WizardDescriptor.Panel<WizardDescriptor> create = Templates.buildSimpleTargetChooser(p, ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC))
                    .bottomPanel(new NotenClasstestWizardPanel()).create();
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
        final AssessmentContext.Factory<StudentId, Int2> factory = (AssessmentContext.Factory<StudentId, Int2>) wizard.getProperty(PROP_ASSESSMENTCONTEXTFACTORY);
        final DataObject template = DataObject.find(Templates.getTemplate(wizard));
        final FileObject folder = Templates.getTargetFolder(wizard);
        final String targetName = Templates.getTargetName(wizard);
        final DataFolder df = DataFolder.findFolder(folder);
        final DataObject newData = template.createFromTemplate(df, targetName);
        final Project project = Templates.getProject(wizard);
        assert project != null;//Project required;
        project.getProjectDirectory().refresh();
        final Unit unit = project.getLookup().lookup(Unit.class);
        if (unit == null) {
            throw new IOException("No unit found in project lookup: " + ProjectUtils.getInformation(project).getName());
        }
        final AssessmentContext<?, Int2> assess = factory.create();
        newData.getLookup().lookup(ClasstestSupport.class).attachContext(assess);
        final Set<Student> students = unit.getStudents();
        NbUtilities.waitAndThen(newData.getLookup(), ClassroomTestEditor2.class, editor -> {
            final EditableClassroomTest test = editor.getEditableClassroomTest();
            students.stream().forEach(test::updateStudent);

            if (newData.isModified()) {
                final Savable save = newData.getLookup().lookup(Savable.class);
                if (save != null) {
                    try {
                        save.save();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        String f = FileUtil.getRelativePath(project.getProjectDirectory(), folder);
        saveTargetFolder(project, f);
        return Collections.singleton(newData);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
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

    @Messages("NotenClasstestWizardIterator.preselectedTargetFolder=Tests")
    private String findTargetFolder(Project p) {
        final LocalFileProperties lfp = p.getLookup().lookup(LocalFileProperties.class);
        String folder;
        if (lfp != null) {
            if ((folder = lfp.getProperty(PRESELECTED_TARGET_FOLDER)) != null) {
                return folder;
            }
        }
        if ((folder = NbPreferences.forModule(NotenClasstestWizardIterator.class).get(PRESELECTED_TARGET_FOLDER, null)) != null) {
            return folder;
        }
        return NbBundle.getMessage(NotenClasstestWizardIterator.class, PRESELECTED_TARGET_FOLDER);
    }

    private void saveTargetFolder(Project p, String f) {
        final LocalFileProperties lfp = p.getLookup().lookup(LocalFileProperties.class);
        if (lfp != null && lfp.getProperty(PRESELECTED_TARGET_FOLDER) != null) {
            return;
        }
        if (NbBundle.getMessage(NotenClasstestWizardIterator.class, PRESELECTED_TARGET_FOLDER).equals(f)) {
            return;
        }
        NbPreferences.forModule(NotenClasstestWizardIterator.class).put(PRESELECTED_TARGET_FOLDER, f);
    }
}
