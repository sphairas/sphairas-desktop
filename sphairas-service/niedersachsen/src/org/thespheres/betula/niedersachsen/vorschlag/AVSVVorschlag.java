/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.vorschlag;

import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.assess.AbstractAssessmentConvention;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.assess.GradeReference;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = AssessmentConvention.class)
public class AVSVVorschlag extends AbstractAssessmentConvention {

    public static final String NAME = "niedersachsen.avsvvorschlag";
    public static final String DISPLAYNAME = "Vorschlag";
    private static final Vorschlag GRADE = new Vorschlag();
    static final AssessmentConvention CONVENTION = GradeFactory.findConvention(NAME);

    public AVSVVorschlag() {
        super(NAME, new String[]{GRADE.getId()});
    }

    @Override
    protected Grade getGrade(String id) {
        return GRADE;
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        String trimText = text.trim();
        for (Grade g : getAllGrades()) {
            if (g.getLongLabel().equalsIgnoreCase(trimText)
                    || g.getShortLabel().equalsIgnoreCase(trimText)) {
                return g;
            }
        }
        throw new GradeParsingException(NAME, text);
    }

    @Override
    public String getDisplayName() {
        return DISPLAYNAME;
    }

    private static class Vorschlag extends AbstractGrade implements GradeReference {

        private Vorschlag() {
            super(NAME, "vorschlag");
        }

        @Override
        public String getShortLabel() {
            return "V.";
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            Grade arg;
            if (formattingArgs.length != 0 && formattingArgs[0] instanceof Grade) {
                arg = (Grade) formattingArgs[0];
                return getDisplayLabel() + arg.getShortLabel();
            }
            return DISPLAYNAME;
        }

        @Override
        public String getDisplayLabel() {
            return NbBundle.getMessage(AVSVVorschlag.class, "avsv.vorschlag.displayLabel");
        }

    }

}
