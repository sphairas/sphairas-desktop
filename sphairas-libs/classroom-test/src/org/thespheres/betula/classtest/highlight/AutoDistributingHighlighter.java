/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.highlight;

import com.google.common.eventbus.Subscribe;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableStudent;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
class AutoDistributingHighlighter extends FontHighlighter implements HighlightPredicate {

    protected ClassroomTestEditor2 editor;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private AutoDistributingHighlighter(Font f) {
        super(f.deriveFont(Font.BOLD));
        this.setHighlightPredicate(this);
    }

    protected boolean ensureEditor(ComponentAdapter ca) {
        if (editor == null) {
            TopComponent tc = findTC(ca.getComponent());
            if (tc != null) {
                ClassroomTestEditor2 e = tc.getLookup().lookup(ClassroomTestEditor2.class);
                if (e != null) {
                    editor = e;
                    editor.getEditableClassroomTest().getEventBus().register(this);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private TopComponent findTC(Component comp) {
        //von ExplorerManger.find.....
        for (;;) {
            if (comp == null) {
                return null;
            }

            if (comp instanceof TopComponent) {
                return (TopComponent) comp;
            }
            comp = comp.getParent();
        }
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        if (!ensureEditor(ca)) {
            return false;
        }
//        Field f = editor.getModel().getFields().getField(ca.row, ca.column);
//        if (f != null && f.getAreaY() == ClassroomTestFields.STUDENT_ROWS && f.getAreaX() == ClassroomTestFields.STUDENT_GRADE_COLUMN) {
//            EditableStudent es = editor.getEditableClassroomTest().getEditableStudents().get(f.getFieldY());
//            return !es.getStudentScores().isAutoDistributing();
//        } 
            return false;
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

    @MimeRegistration(mimeType = "text/betula-classtest-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new AutoDistributingHighlighter(table.getFont());
        }
    }
}
