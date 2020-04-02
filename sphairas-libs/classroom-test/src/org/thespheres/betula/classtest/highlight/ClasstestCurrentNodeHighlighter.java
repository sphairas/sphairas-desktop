/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.highlight;

import java.util.Optional;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.openide.windows.TopComponent;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.ui.swingx.AbstractCurrentNodeHighlighter;

/**
 *
 * @author boris.heithecker
 */
abstract class ClasstestCurrentNodeHighlighter extends AbstractCurrentNodeHighlighter<EditableProblem> {

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    ClasstestCurrentNodeHighlighter(JXTable table, TopComponent tc, boolean surviveFocusChange) {
        super(table, tc, surviveFocusChange, EditableProblem.class);
    }

    protected Optional<EditableProblem> findAssessmentProvider(ComponentAdapter ca) {
        int index = ca.convertColumnIndexToModel(ca.column) - 1;
        EditableClassroomTest<?, ?, ?>  tr = component.getLookup().lookup(EditableClassroomTest.class);
        if (tr != null && index >= 0 && index < tr.getEditableProblems().size()) {
            return Optional.of(tr.getEditableProblems().get(index));
        }
        return Optional.empty();
    }

}
