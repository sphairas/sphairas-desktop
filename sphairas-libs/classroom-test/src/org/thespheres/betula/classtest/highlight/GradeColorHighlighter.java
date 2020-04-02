/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.highlight;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import javax.swing.AbstractAction;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.ui.swingx.ExternalizableHighlighter;
import org.thespheres.betula.ui.swingx.ExternalizableHighlighter.ExternalizableHighlighterInstanceFactory;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
@Messages({"LBL_HighlighterUp=HL Up",
    "LBL_HighlighterDown=HL Down"})
class GradeColorHighlighter extends ColorHighlighter implements ExternalizableHighlighter, HighlightPredicate, LookupListener {

    public static final long serialVersionUID = 1l;
    @ActionRegistration(displayName = "#LBL_HighlighterUp", surviveFocusChange = false)
    @ActionID(category = "Betula", id = "org.thespheres.betula.classtest.highlight.HighlighterUp")
    @ActionReferences({
        @ActionReference(path = "Shortcuts", name = "O-UP")})
    public static final String HIGHLIGHTER_UP_KEY = "highlighterUp";
    @ActionRegistration(displayName = "#LBL_HighlighterDown", surviveFocusChange = false)
    @ActionID(category = "Betula", id = "org.thespheres.betula.classtest.highlight.HighlighterDown")
    @ActionReferences({
        @ActionReference(path = "Shortcuts", name = "O-DOWN")})
    public static final String HIGHLIGHTER_DOWN_KEY = "highlighterDown";
    private Grade currentGrade;
    public static final String ID = "GradeColorHighlighter";
    private TopComponent component;
    private Lookup.Result<ClassroomTestEditor2> result;
    private ClassroomTestEditor2 editor;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public GradeColorHighlighter() {
        setBackground(Color.ORANGE);
        setSelectedBackground(Color.RED);
        setHighlightPredicate(this);
    }

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private GradeColorHighlighter(TopComponent tc) {
        this();
        component = tc;
        init();
    }

    protected void init() {
        this.result = component.getLookup().lookupResult(ClassroomTestEditor2.class);
        this.result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        if (editor != null) {
            editor.getEditableClassroomTest().getEventBus().unregister(this);
        }
        editor = component.getLookup().lookup(ClassroomTestEditor2.class);
        if (editor != null) {
            editor.getEditableClassroomTest().getEventBus().register(this);
            component.getActionMap().put("highlighterUp", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doUpAction();
                }
            });
            component.getActionMap().put("highlighterDown", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doDownAction();
                }
            });
        }
    }

    @Override
    public boolean isHighlighted(Component comp, ComponentAdapter ca) {
        Object so = ca.getValue(0);
        if (currentGrade != null && so instanceof EditableStudent) {
            EditableStudent es = (EditableStudent) so;
            Grade test = es.getStudentScores().getGrade();
            if (test instanceof Grade.Biasable) {
                test = ((Grade.Biasable) test).getUnbiased();
            }
            return Objects.equals(test, currentGrade);
        }
        return false;
    }

    private void doUpAction() {
        if (editor != null) {
            final AssessmentConvention ac = editor.getAssessmentConvention();
            final Grade restart = ac instanceof AssessmentConvention.OfBiasable
                    ? ((AssessmentConvention.OfBiasable) ac).getFloorUnbiased()
                    : ac.getAllGrades()[0];
            currentGrade = (currentGrade != null ? currentGrade.getNextHigher() : restart);
            fireStateChanged();
        }
    }

    private void doDownAction() {
        if (editor != null) {
            final AssessmentConvention ac = editor.getAssessmentConvention();
            final Grade restart = ac instanceof AssessmentConvention.OfBiasable
                    ? ((AssessmentConvention.OfBiasable) ac).getCeilingUnbiased()
                    : ac.getAllGradesReverseOrder()[0];
            currentGrade = (currentGrade != null ? currentGrade.getNextLower() : restart);
            fireStateChanged();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(currentGrade);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object o = in.readObject();
        if (o instanceof Grade) {
            currentGrade = (Grade) o;
        }
    }

    @Override
    public void restore(JXTable table, TopComponent tc) {
        component = tc;
        init();
    }

    @Subscribe
    public void onModelChange(CollectionChangeEvent event) {
        if (event.getCollectionName().equals(EditableClassroomTest.COLLECTION_STUDENTS)) {
            fireStateChanged();
        }
    }

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (evt.getSource() instanceof EditableStudent && EditableStudent.PROP_GRADE.equals(name)) {
            fireStateChanged();
        }
    }

    @Override
    public String id() {
        return ID;
    }

    @MimeRegistration(mimeType = "text/betula-classtest-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory implements ExternalizableHighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new GradeColorHighlighter(tc);
        }

        @Override
        public String id() {
            return ID;
        }
    }
}
