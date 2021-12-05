/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.sonderreg;

import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.AbstractAssessmentConvention;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = AssessmentConvention.class)
public class ASVAssessmentConventionCorona extends AbstractAssessmentConvention implements AssessmentConvention.OfBiasable {

    public static final String NAME = "niedersachsen.avsv.corona2021";
    public static final String[] GRADES = {"av", "sv"};
    private final Map<String, Grade> map = new HashMap<>();

    public ASVAssessmentConventionCorona() {
        super(NAME, GRADES);
    }

    @Override
    protected Grade getGrade(String id) {
        if (map.isEmpty()) {
            for (final String g : GRADES) {
                map.put(g, new CoronaKopfnote(getName(), g));
            }
        }
        return map.get(id);
    }

    @Override
    public Grade[] getAllGradesUnbiased() {
        return getAllGrades();
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        final String trimText = text.trim();
        for (final Grade g : getAllGrades()) {
            if (g.getLongLabel().equalsIgnoreCase(trimText)
                    || g.getShortLabel().equalsIgnoreCase(trimText)) {
                return g;
            }
        }
        throw new GradeParsingException(getName(), text);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ASVAssessmentConventionCorona.class, "niedersachsen.avsv.corona2021.displayName");
    }

    class CoronaKopfnote extends AbstractGrade {

        CoronaKopfnote(final String gradeConvention, final String gradeId) {
            super(gradeConvention, gradeId);
        }

        @Override
        public String getShortLabel() {
            return "corona." + gradeId.toUpperCase();
        }

        @Override
        public String getLongLabel(final Object... formattingArgs) {
            final String k = "niedersachsen.avsv.corona2021.longlabel." + getId();
            return NbBundle.getMessage(CoronaKopfnote.class, k);
        }

    }
}
