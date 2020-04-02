/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.Lookup;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
public class GradeComboBoxModel extends AbstractConventionComboBox<AssessmentConvention, Grade> {

    public GradeComboBoxModel() {
        super(null, true, true);
    }

    public GradeComboBoxModel(String preferredConvention) {
        super(new String[]{preferredConvention}, true, true);
    }

    public GradeComboBoxModel(String preferredConvention, boolean addNull) {
        super(new String[]{preferredConvention}, true, addNull);
    }

    public GradeComboBoxModel(String[] conventions) {
        super(conventions, false, true);
    }

    public GradeComboBoxModel(String[] conventions, boolean addNull) {
        super(conventions, false, addNull);
    }

    @Override
    protected List<AssessmentConvention> allConventions() {
        return Lookup.getDefault().lookupAll(AssessmentConvention.class).stream().map(AssessmentConvention.class::cast).collect(Collectors.toList());
    }

    @Override
    protected List<Grade> allTags(AssessmentConvention convention) {
        return Arrays.stream(convention.getAllGrades()).map(Grade.class::cast).collect(Collectors.toList());
    }

}
