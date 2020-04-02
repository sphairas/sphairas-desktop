/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.mitarbeit.collect;

import java.util.Iterator;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = AssessmentConvention.class)
public class Collect implements AssessmentConvention {

    static final String CONVENTION = "assess.collect";
    public static Grade[] ALL = CollGrade.ALL;

    @Override
    public Grade[] getAllGrades() {
        return ALL;
    }

    @Override
    public Grade[] getAllGradesReverseOrder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Grade> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        String trimText = text.trim();
        for (Grade g : CollGrade.ALL) {
            if (g.getLongLabel().equals(trimText)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public Grade find(String id) {
        for (Grade g : CollGrade.ALL) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        throw new IllegalArgumentException("No such grade.");
    }

    @Override
    public String getName() {
        return CONVENTION;
    }

    @Override
    public String getDisplayName() {
        return "Einsammeln";
    }
}
