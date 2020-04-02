/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thespheres.betula.mitarbeit;

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
public class Hausaufgaben implements AssessmentConvention {
    
    static final String CONVENTION = "Hausaufgaben";
    public static Grade[] ALL = HAGrade.ALL;

    @Override
    public Grade[] getAllGrades() {
        return ALL;
    }

    @Override
    public Grade[] getAllGradesReverseOrder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Grade[] getAllLinkedGrades() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getGradeCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Grade getHighestLinkedGrade() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Grade getLowestLinkedGrade() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Grade> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<Grade> linkedGradesIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator<Grade> linkedGradesReverseIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        String trimText = text.trim();
        for (Grade g : HAGrade.ALL) {
            if (g.getLongLabel().equals(trimText)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public Grade find(String id) {
        for (Grade g : HAGrade.ALL) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        throw new IllegalArgumentException("No such grade.");
    }

    @Override
    public String getName() {
        return "Hausaufgaben";
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

}
