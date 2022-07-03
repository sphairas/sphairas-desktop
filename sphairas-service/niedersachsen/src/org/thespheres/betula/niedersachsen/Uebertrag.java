/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.AbstractAssessmentConvention;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;
import org.thespheres.betula.assess.GradeReference;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.PrecedingTermGradeReference;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Uebertrag.displayName=Übertrag",
    "Uebertrag.format.uebertrag.label=Note aus dem {0}. Schulhalbjahr"})
@ServiceProvider(service = AssessmentConvention.class)
public class Uebertrag extends AbstractAssessmentConvention {

    public static final String NAME = "niedersachsen.uebertrag";
    private static final UebertragGrade GRADE = new UebertragGrade();

    public Uebertrag() {
        super(NAME, new String[]{GRADE.getId()});
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(Uebertrag.class, "Uebertrag.displayName");
    }

    @Override
    protected Grade getGrade(String id) {
        return id.equals(GRADE.getId()) ? GRADE : null;
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

    private static class UebertragGrade extends AbstractGrade implements GradeReference, PrecedingTermGradeReference {

        UebertragGrade() {
            super(NAME, "uebertrag");
        }

        @Override
        public TermId findPrecedingTermId(TermId original) throws IllegalAuthorityException {
            if (!original.getAuthority().equals(LSchB.AUTHORITY)) {
                IllegalAuthorityException illaex = new IllegalAuthorityException();
                illaex.setIllegalIdentity(original);
                throw illaex;
            }
            if (original.getId() % 2 != 0) {//Vornoten nur für das zweite Halbjahr
                return null;
            }
            return new TermId(LSchB.AUTHORITY, original.getId() - 1);
        }

        @Override
        public String getShortLabel() {
            return NbBundle.getMessage(Uebertrag.class, "niedersachsen.uebertrag.label");
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            Grade arg;
            if (formattingArgs.length != 0 && formattingArgs[0] instanceof Grade) {
                arg = (Grade) formattingArgs[0];
                return arg.getShortLabel() + getDisplayLabel();
            }
            return NbBundle.getMessage(Uebertrag.class, "niedersachsen.uebertrag.long.label");
        }

        @Override
        public String getDisplayLabel() {
            return NbBundle.getMessage(Uebertrag.class, "niedersachsen.uebertrag.displayLabel");
        }

    }

}
