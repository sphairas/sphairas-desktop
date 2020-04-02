/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

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
public class Teilnahme extends AbstractAssessmentConvention {

    public static final String NAME = "niedersachsen.teilnahme";
    public static final String[] ID = {"tg", "ntg"};

    public Teilnahme() {
        super(NAME, ID);
    }

    @Override
    protected Grade getGrade(String id) {
        return new TeilnahmeGrade(id);
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        String trimText = text.trim();
        for (Grade g : getAllGrades()) {
            if (g.getLongLabel().equalsIgnoreCase(trimText) || g.getShortLabel().equalsIgnoreCase(trimText)) {
                return g;
            }
        }
        throw new GradeParsingException(NAME, text);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(Teilnahme.class, "niedersachsen.teilnahme.display");
    }

    private final class TeilnahmeGrade extends AbstractGrade {

        public TeilnahmeGrade(String gradeId) {
            super(NAME, gradeId);
        }

        @Override
        public String getShortLabel() {
            switch (getId()) {
                case "tg":
                    return "teilg.";
                case "ntg":
                    return "n. teilg.";
                default:
                    return getId();
            }
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            switch (getId()) {
                case "tg":
                    return "teilgenommen";
                case "ntg":
                    return "nicht teilgenommen";
                default:
                    return getId();
            }
        }

    }
}
