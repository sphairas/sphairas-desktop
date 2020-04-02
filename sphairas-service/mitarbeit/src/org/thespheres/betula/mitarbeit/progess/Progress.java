/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.mitarbeit.progess;

import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = AssessmentConvention.class)
public class Progress implements AssessmentConvention {
    
    public static final String NAME = "progress";
    public static final String ABSENT = "absent";

    @Override
    public Grade[] getAllGrades() {
        return new Grade[]{};
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Grade find(String id) {
        return new ProgressGrade(id);
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getName() {
        return NAME;
    }
    
}
